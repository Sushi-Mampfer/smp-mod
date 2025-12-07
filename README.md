# Smp Mod(very creative I know)
The framework for your next smp, it's heavily inspired by craft attack 13 and aims to make your next two minecraft phase last longer.

# Features/Config
```
noEnd: false // disables the end
noNether: false // disables the nether
noRockets: false // disables rockets in the overworld
noSleep: false // enables the nosleep command(tells everyone that goes to bed that you don't want the mto sleep)
spawnElytra:
    enabled: false // enables the spawnelytra
    radius: 0 // radius around the worldspawn where the spawnelytra can be used
speedyGhast: false // the speedy ghast enchantment makes ghasts faster
whitelist:
    channel: '' // the channelid where the players should whitelist themself
    enabled: false // allows players to whitelist themself through discord
    token: '' // your discord bots token, it needs to have manage webhooks, delete messages and add reactions perms in the channel
    verification: // enables extra verification(more than 1 account, new discord user and banned users)
    webhook_id: '' // the id of your webhook, not the whole webhook, only the part between the second to last and last /
    whitelist_role: '' // the id for the role that can verify users that get caught by the verification feature

```
