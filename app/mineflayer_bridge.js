/**
 * Mineflayer Android IPC WebSocket Bridge
 * Protocol version: 1.2.0
 *
 * This script runs in embedded Node.js runtime / local environment
 * and communicates via JSON WebSocket packets with the Android native app.
 */

const WebSocket = require('ws');
const mineflayer = require('mineflayer');

const PORT = process.env.BRIDGE_PORT || 8080;
const wss = new WebSocket.Server({ port: PORT });

console.log(`[Mineflayer Bridge] WebSocket IPC server listening on port ${PORT}`);

let currentBot = null;
let afkInterval = null;
let reconnectTimeout = null;
let currentConfig = null;
let botUptimeStart = null;

function broadcast(action, payload) {
  const message = JSON.stringify({ action, payload, timestamp: Date.now() });
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

function startAntiAfkLoop(bot, config) {
  if (afkInterval) clearInterval(afkInterval);
  if (!config.antiAFK) return;

  const intervalMs = (config.rotationIntervalSeconds || 15) * 1000;
  console.log(`[Anti-AFK] Initialized cycle every ${intervalMs}ms`);

  afkInterval = setInterval(() => {
    if (!bot || !bot.entity) return;

    try {
      // 1. Random look angle (Yaw & Pitch jitter)
      const yaw = Math.random() * Math.PI * 2 - Math.PI;
      const pitch = (Math.random() * 0.6 - 0.3); // slight up/down
      bot.look(yaw, pitch, true);
      broadcast('AFK_ACTION', { type: 'LOOK', yaw, pitch });

      // 2. Micro movement (Auto-walk step)
      if (config.autoWalk) {
        bot.setControlState('forward', true);
        setTimeout(() => {
          bot.setControlState('forward', false);
          broadcast('AFK_ACTION', { type: 'WALK_STEP' });
        }, 300);
      }

      // 3. Sneak toggle
      if (config.autoSneak) {
        bot.setControlState('sneak', true);
        setTimeout(() => bot.setControlState('sneak', false), 400);
      }

      // 4. Auto-eat check
      if (config.autoEat && bot.food <= (config.autoEatThreshold || 14)) {
        const foodItem = bot.inventory.items().find(item => item.name.includes('apple') || item.name.includes('bread') || item.name.includes('cooked') || item.name.includes('carrot') || item.name.includes('steak'));
        if (foodItem) {
          bot.equip(foodItem, 'hand').then(() => {
            bot.consume().then(() => {
              broadcast('AFK_ACTION', { type: 'EAT', food: bot.food });
            }).catch(() => {});
          }).catch(() => {});
        }
      }
    } catch (err) {
      console.error('[Anti-AFK Error]', err.message);
    }
  }, intervalMs);
}

function connectBot(payload) {
  if (currentBot) {
    disconnectBot('Reconnecting with new configuration');
  }

  currentConfig = payload;
  console.log(`[Mineflayer Bridge] Connecting to ${payload.host}:${payload.port} as ${payload.username} (${payload.auth})`);

  broadcast('STATUS_CHANGED', {
    state: 'CONNECTING',
    host: payload.host,
    port: payload.port,
    username: payload.username,
    message: `Connecting to ${payload.host}:${payload.port}...`
  });

  const botOptions = {
    host: payload.host,
    port: payload.port || 25565,
    username: payload.username,
    auth: payload.auth || 'offline',
    version: payload.version && payload.version !== 'auto' ? payload.version : false,
    hideErrors: false,
    checkTimeoutInterval: 30000
  };

  if (payload.auth === 'microsoft') {
    botOptions.onMsaCode = (data) => {
      console.log('[Microsoft Auth] Device Code:', data.user_code);
      broadcast('OAUTH_DEVICE_CODE', {
        userCode: data.user_code,
        deviceCode: data.device_code,
        verificationUrl: data.verification_uri,
        expiresIn: data.expires_in,
        message: data.message
      });
    };
  }

  try {
    currentBot = mineflayer.createBot(botOptions);
  } catch (err) {
    broadcast('STATUS_CHANGED', {
      state: 'ERROR',
      error: err.message
    });
    return;
  }

  currentBot.on('login', () => {
    botUptimeStart = Date.now();
    console.log(`[Mineflayer] Logged in successfully!`);
    broadcast('STATUS_CHANGED', {
      state: 'AUTHENTICATING',
      message: 'Logged in! Awaiting world spawn...'
    });
  });

  currentBot.on('spawn', () => {
    console.log(`[Mineflayer] Spawned in world!`);
    broadcast('STATUS_CHANGED', {
      state: 'ONLINE',
      host: payload.host,
      port: payload.port,
      username: currentBot.username,
      ping: currentBot.player ? currentBot.player.ping : 0,
      health: currentBot.health || 20,
      food: currentBot.food || 20,
      dimension: currentBot.game ? currentBot.game.dimension : 'overworld',
      position: currentBot.entity ? {
        x: Math.round(currentBot.entity.position.x),
        y: Math.round(currentBot.entity.position.y),
        z: Math.round(currentBot.entity.position.z)
      } : { x: 0, y: 64, z: 0 },
      message: 'Bot is active in world'
    });

    startAntiAfkLoop(currentBot, currentConfig);
  });

  currentBot.on('message', (jsonMsg, position) => {
    const rawText = jsonMsg.toAnsi ? jsonMsg.toString() : String(jsonMsg);
    const motdFormatted = jsonMsg.toMotd ? jsonMsg.toMotd() : rawText;

    let sender = 'System';
    let type = 'SYSTEM';

    if (rawText.startsWith('<') && rawText.includes('>')) {
      sender = rawText.substring(1, rawText.indexOf('>'));
      type = 'PLAYER';
    } else if (rawText.toLowerCase().includes('whispers:') || rawText.toLowerCase().includes('-> me')) {
      type = 'WHISPER';
    }

    broadcast('CHAT_MESSAGE', {
      rawText: motdFormatted || rawText,
      plainText: rawText,
      sender: sender,
      type: type,
      timestamp: Date.now()
    });
  });

  currentBot.on('health', () => {
    if (!currentBot) return;
    broadcast('HEALTH_UPDATE', {
      health: currentBot.health,
      food: currentBot.food
    });
  });

  currentBot.on('kicked', (reason) => {
    const kickReason = typeof reason === 'object' ? JSON.stringify(reason) : String(reason);
    console.log('[Mineflayer] Kicked:', kickReason);
    broadcast('STATUS_CHANGED', {
      state: 'DISCONNECTED',
      message: `Kicked from server: ${kickReason}`
    });
    handleAutoReconnect();
  });

  currentBot.on('error', (err) => {
    console.error('[Mineflayer] Error:', err.message);
    broadcast('BOT_ERROR', {
      error: err.message
    });
  });

  currentBot.on('end', (reason) => {
    console.log('[Mineflayer] Connection ended:', reason);
    broadcast('STATUS_CHANGED', {
      state: 'DISCONNECTED',
      message: `Disconnected: ${reason || 'Connection closed'}`
    });
    if (afkInterval) clearInterval(afkInterval);
    handleAutoReconnect();
  });
}

function handleAutoReconnect() {
  if (afkInterval) clearInterval(afkInterval);
  if (currentConfig && currentConfig.autoReconnect) {
    const delay = currentConfig.reconnectDelaySeconds || 10;
    console.log(`[Mineflayer Bridge] Reconnecting in ${delay}s...`);
    broadcast('STATUS_CHANGED', {
      state: 'RECONNECTING',
      message: `Reconnecting in ${delay}s...`
    });
    reconnectTimeout = setTimeout(() => {
      if (currentConfig) connectBot(currentConfig);
    }, delay * 1000);
  }
}

function disconnectBot(reason = 'User requested disconnect') {
  if (reconnectTimeout) clearTimeout(reconnectTimeout);
  if (afkInterval) clearInterval(afkInterval);
  if (currentBot) {
    try {
      currentBot.quit(reason);
    } catch (e) {}
    currentBot = null;
  }
  broadcast('STATUS_CHANGED', {
    state: 'DISCONNECTED',
    message: reason
  });
}

wss.on('connection', (ws) => {
  console.log('[Mineflayer Bridge] Android client connected to IPC socket');

  // Send initial state snapshot
  ws.send(JSON.stringify({
    action: 'INIT_STATE',
    payload: {
      connected: !!currentBot,
      state: currentBot ? 'ONLINE' : 'DISCONNECTED',
      uptime: botUptimeStart ? Math.floor((Date.now() - botUptimeStart) / 1000) : 0,
      config: currentConfig
    }
  }));

  ws.on('message', (raw) => {
    try {
      const data = JSON.parse(raw);
      console.log('[Mineflayer IPC Inbound]', data.action);

      switch (data.action) {
        case 'CONNECT_BOT':
          connectBot(data.payload);
          break;

        case 'DISCONNECT_BOT':
          disconnectBot('Disconnected from app UI');
          break;

        case 'SEND_CHAT':
          if (currentBot && data.payload && data.payload.message) {
            currentBot.chat(data.payload.message);
          }
          break;

        case 'UPDATE_ANTI_AFK':
          if (currentConfig) {
            Object.assign(currentConfig, data.payload);
            if (currentBot) startAntiAfkLoop(currentBot, currentConfig);
          }
          break;

        case 'GET_STATUS':
          ws.send(JSON.stringify({
            action: 'STATUS_RESPONSE',
            payload: {
              online: !!currentBot,
              health: currentBot ? currentBot.health : 0,
              food: currentBot ? currentBot.food : 0,
              ping: currentBot && currentBot.player ? currentBot.player.ping : 0
            }
          }));
          break;
      }
    } catch (e) {
      console.error('[Mineflayer IPC Error Parsing Message]', e.message);
    }
  });
});
