# MineStoreRecode
A recode of the MineStore plugin for Minecraft servers.
Download the plugin from my [JENKINS](https://js.chrommob.fun) server here: [MineStoreRecode](https://js.chrommob.fun/job/MineStore/)
# Table of Contents
1. [Configuration](#Configuration)
2. [Permissions](#Permissions)
3. [Placeholders](#Placeholders)
4. [Addons](#Addons)
## Configuration
### config.yml
```yaml
# MineStore Configuration
# This is the configuration file for MineStore.
# Only enable debug if requested by a MineStore developer. 
debug: false
# This is the language that is used by the plugin. This config option is required. Available languages: en_US, cs_CZ, ru_RU, ua_UA but you can create your own language file.
language: en_US
# Store URL is the URL to your MineStore website. This config option is required.
store-url: "https://store.example.com"
weblistener:
  secret-enabled: false
  # This is the secret key that is used to authenticate the webhooks. This config option is required if secret-enabled is set to true.
  secret-key: extraSecretKey
api:
  key-enabled: false
  # This is the secret key that is used to authenticate the API. This config option is required if key-enabled is set to true.
  key: apiSecretKey
# From this point on, all config options are optional.
auth:
  # This is the amount of time in seconds that the player has to authenticate their Minecraft account with MineStore website.
  timeout: 300
store-command:
  # If this config option is set to true, the player will be sent a link to the MineStore website when they run the /minestore store command.
  enabled: false
mysql:
  # If this config option is set to true, the plugin will use MySQL database to store data.
  enabled: true
  # This is the host of the MySQL database. This config option is required if MySQL is enabled.
  ip: localhost
  # This is the port of the MySQL database. This config option is required if MySQL is enabled.
  port: 3306
  # This is the username of the MySQL database. This config option is required if MySQL is enabled.
  username: root
  # This is the password of the MySQL database. This config option is required if MySQL is enabled.
  password: superSecretPassword
  # This is the name of the MySQL database. This config option is required if MySQL is enabled.
  database: minestore
buy-gui:
  # If this config option is set to true, the player will be able to buy packages from the GUI.
  enabled: true
  # All of the following config options are optional and are used to customize the GUI.
  back:
    # Item that is used to go back to the previous menu.
    item: BARRIER
    # Name of the item that is used to go back to the previous menu.
    name: <red>Back
    # Lore of the item that is used to go back to the previous menu.
    description: <red>Go back to the previous menu!
  category:
    # Title of the GUI that is used to display categories.
    title: <bold><blue><obfuscated>TESTTESTTESTTESTTEST
    # Name of the item that is used to go to the category menu.
    name: <gold>%category%
  subcategory:
    # Title of the GUI that is used to display subcategories.
    title: <red><bold>%category%
    # Name of the item that is used to open the subcategory menu.
    name: <gold>%subcategory%
  package:
    # Title of the GUI that is used to display packages.
    title: <red><bold>%subcategory%
    # Name of the item that is used to open the package menu.
    name: <gold>%package%
    # Lore of the item that is used to open the package menu.
    description: <white>%description%
    # Format of the price of the package.
    price: '<green>Price: </green><gold>%price%USD</gold>'
```
## Language
### en_US.yml
```yaml
auth:
  # Message that is sent to the player when they try to authenticate their Minecraft account with MineStore website.
  initial-message: <dark_green>You are trying to login in to our store. <click:run_command:/ms
    auth><bold><gold>CLICK HERE</gold></bold></click> to confirm authorization! If
    you are not able to click run /minestore auth.
  # Message that is sent to the player when they successfully authenticate their Minecraft account with MineStore website.
  success-message: <dark_green>You have successfully logged in to our store!</dark_green>
  # Message that is sent to the player when they try to authenticate their Minecraft account with MineStore website but they do not have a pending auth.
  failure-message: <dark_red>You do not have pending auth!</dark_red>
  # Message that is sent to the player when they fail to authenticate their Minecraft account with MineStore website due to timeout.
  timeout-message: <dark_red>You have failed to log in to our store!</dark_red>
store-command:
  # Message that is sent to the player when they run the /minestore store command.
  message: <dark_green>Visit our store <click:open_url:%store_url%><hover:show_text:'<gold>Click
    to open the store!'><bold><gold>here</gold></bold></hover></click>!</dark_green>
buy-gui:
  # Message that is sent to the player when they click on package item.
  message: <dark_green>To buy <red><bold>%package%</bold></red> click <click:open_url:%buy_url%><bold><gold>HERE</gold></bold></click>!
```
## Permissions
- **minestore.auth**
  - Allows the player to use the `/minestore auth` command. This command allows the player to authenticate their Minecraft account with MineStore website.
- **minestore.buy**
  - Allows the player to use the `/buy` command. This command allows the player to display GUI.
- **minestore.reload**
  - Allows the player to use the `/minestore reload` command. This command reloads the config.yml file.
- **minestore.setup**
  - Allows the player to use the `/minestore setup` command. This command allows the player to edit the config.yml file in-game.
- **minestore.store**
  - Allows the player to use the `/store` command. This command sends the player link to the MineStore website.
## Placeholders
- TopDonators
  - **%ms_top_donator_username_number%**
    - Example: %ms_top_donator_username_1% will return the username with the most amount of money donated.
    - Example result: `Notch`
  - **%ms_top_donator_price_number%**
    - Example: %ms_top_donator_price_1% will return the amount of money donated by the user with the most amount of money donated.
    - Example result: `1000000`
- LastDonators
  - **%ms_last_donator_username_number%**
    - Example: %ms_last_donator_username_1% will return the username of the last person to donate.
    - Example result: `Steve`
  - **%ms_last_donator_price_number%**
    - Example: %ms_last_donator_price_1% will return the amount of money donated by the last person to donate.
    - Example result: `100`
  - **%ms_last_donator_package_number%**
    - Example: %ms_last_donator_package_1% will return the name of the package the last person to donate bought.
    - Example result: `VIP`
- DonationGoal
  - **%ms_donation_goal_current%**
    - Example: %ms_donation_goal_current% will return the current amount of money donated.
    - Example result: `400`
  - **%ms_donation_goal_target%**
    - Example: %ms_donation_goal_target% will return the target amount of money to be donated.
    - Example result: `1000`
  - **%ms_donation_goal_percentage%**
    - Example: %ms_donation_goal_percentage% will return the percentage of the donation goal that has been reached.
    - Example result: `40`
  - **%ms_donation_goal_bar_number%**
    - Example: %ms_donation_goal_bar_10% will return a bar that represents the percentage of the donation goal that has been reached.
    - Example result if percentage is 50%: ![image](https://user-images.githubusercontent.com/62996347/225689985-d6ee5fbc-a80b-484b-908e-046c2869f784.png)

## Addons
### Where to put addons
Addons should be placed in the `plugins/MineStore/addons` folder.

### Where to get addons
You can get the official addon from the same website as the plugin from my [JENKINS](https://js.chrommob.fun) server here: [MineStore Addon](https://js.chrommob.fun/job/MineStoreAddons/)

### What does the official addon do?
The official addon adds the following features:
- **UserInfo**
  - This feature allows you to display all the information about the player in the GUI. The packages they purchased, how many times they donated, how much money they donated, etc.
- **Announcements**
  - This feature allows you to send customizable message to players when someone donates, purchases a package, etc.
- **CustomEconomy**
  - This feature allows you to use MineStore own economy instead of Vault. This is useful if you want to use virtual currency separate from your server economy.
The addon is also very secure because everything is stored in remote database and encrypted with AES and RSA just like every communication the addon does.

### Configuring the addon
The addon is configured in the `plugins/MineStore/addons/<addon_name>/config.yml` file.
```yaml
purchase-announcer:
  # Enables or disables the purchase announcer. If enabled, the plugin will send the message below to all players when someone purchases a package.
  enabled: true
  # Message that is sent to all players when someone purchases a package.
  format: <red><bold>%player%</bold></red> bought <bold>%package%</bold> for <gold>%price%
user-info:
  # Enables or disables the user info. If enabled, the plugin will open the GUI with all the information about the player when someone executes the /user <player> command.
  # /user requires permission: minestoreaddons.userinfo.self to open GUI about yourself. /user <player> command requires permission: minestoreaddons.userinfo.other to open GUI about other players.
  enabled: true
  # Message that is sent to the player when the player they are trying to open the GUI about did not buy any packages yet.
  not-found: <red>%player% did not buy any packages yet.
  gui:
    # Title of the GUI.
    title: <gold>%player%'s packages
    # Name of the item that is used to display the package name.
    item: PAPER
economy:
  # Enables or disables the custom economy. If enabled, the plugin will use MineStore own economy instead of Vault.
  enabled: true
  message:
    # Message that is sent to the player when they execute the /mse balance command. %amount% is replaced with the amount of money the player has.
    # Requires permission: ms.economy.balance
    balance: <red>Your balance is</red> <white><bold>%amount%</bold>.
    # Message that is sent to the player when they execute the /mse add <amount> command. 
    # %amount% is replaced with the amount of money the player has added to the player's balance.
    # Requires permission: ms.economy.add.self
    add: <gold>Added %amount% to your balance
    # Message that is sent to the player when they execute the /mse add <player> <amount> command.
    # %amount% is replaced with the amount of money the player has added to the player's balance.
    # %player% is replaced with the name of the player the money was added to.
    # Requires permission: ms.economy.add.other
    add-other: <gold>Added %amount% to %player%'s balance
    # Message that is sent to the player when they execute the /mse remove <amount> command.
    # %amount% is replaced with the amount of money the player has removed from the player's balance.
    # Requires permission: ms.economy.remove.self
    remove: <gold>Removed %amount% from your balance
    # Message that is sent to the player when they execute the /mse remove <player> <amount> command.
    # %amount% is replaced with the amount of money the player has removed from the player's balance.
    # %player% is replaced with the name of the player the money was removed from.
    # Requires permission: ms.economy.remove.other
    remove-other: <gold>Removed %amount% from %player%'s balance
    # Message that is sent to the player when they execute the /mse set <amount> command.
    # %amount% is replaced with the amount of money the player has set their balance to.
    # Requires permission: ms.economy.set.self
    set: <gold>Set your balance to %amount%
    # Message that is sent to the player when they execute the /mse set <player> <amount> command.
    # %amount% is replaced with the amount of money the player has set the player's balance to.
    # %player% is replaced with the name of the player the money was set to.
    # Requires permission: ms.economy.set.other
    set-other: <gold>Set %player%'s balance to %amount%
    # Message that is sent to the player when they execute the /mse send <player> <amount> command.
    # %amount% is replaced with the amount of money the player has sent to the player.
    # %player% is replaced with the name of the player the money was sent to.
    # Requires permission: ms.economy.send
    send: <gold>Sent %amount% to %player%
    # Message that is sent to the receiver when they receive money from the /mse send <player> <amount> command.
    # %amount% is replaced with the amount of money the player was sent.
    # %player% is replaced with the name of the player the money was sent by.
    # Requires permission: ms.economy.send
    send-other: <gold>Received %amount% from %player%
manual-redeem:
  # Enables or disables the manual redeem. If enabled, the plugin will send the message below to the player when they have packages to redeem.
  enabled: true
  # Enables or disables the message that is sent to the player when they have join the server and have packages to redeem.
  message-enabled: true
  # Message that is sent to the player when they have join the server and have packages to redeem.
  message: <gold>You have %amount% packages to redeem. Run <bold>/redeem</bold> to redeem them.
  # Message that is sent to the player when they run the /redeem command and have no packages to redeem.
  no-packages: <gold>You have no packages to redeem.
  gui:
    # Title of the GUI.
    title: <gold>Redeem packages
    # Name of the item that is used to display the package in the GUI.
    item: PAPER
```
