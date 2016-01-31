# Quick Dice Roller
Quick Dice Roller is an app made for the tabletop RPG Geek. It make possible to emulate almost all the complex dice rolls required by those games.

Check the official page on the [Play Store](https://play.google.com/store/apps/details?id=ohm.quickdice) for screenshot and details.

For any question, contact me or (better) write a post to [the official community](https://plus.google.com/communities/103741122374648637652).

## Latest signed build

Get latest signed build [here](https://github.com/Ohmnibus/quick-dice-roller/blob/master/deploy/quickDiceRoller-release.apk?raw=true).

This is a beta version and may contain some bugs - please report them asap.

You can also [become a beta tester](http://goo.gl/UuJBSO).

## Screenshots
|Main screen|Collections|
|----|----|
|<img src="/deploy/SS-01.jpg?raw=true" width="240" height="400" />|<img src="/deploy/SS-02.jpg?raw=true" width="240" height="400" />|

|Die editor|Expression builder|
|----|----|
|<img src="/deploy/SS-07.jpg?raw=true" width="240" height="400" />|<img src="/deploy/SS-05.jpg?raw=true" width="240" height="400" />|

## Project Structure

The project is structured in different modules. Each one contains a (customized) library. Check the README file on each folder for further details.

Main module is located in the [quickDiceRoller](/quickDiceRoller) folder.

### Special Modules

[quickDiceRoller](/quickDiceRoller): This is the main project.

[diceExpression](/diceExpression): This is the library that parse and evaluate string expressions (formulas) representing dice rolls.

[deploy](/deploy): This folder contain assets, screenshots and descriptions used to deploy the app on different app stores. This is not needed to build the app.

## Translators Needed
Anyone, developer or not, can help to improve Quick Dice Roller by translating text or giving feed-back on current translations.

Please check the [Quick Dice Roller project hosted on Crowdin](https://crowdin.com/project/quick-dice-roller). You can register in no more than few minutes, and even the translation of a single string is a valuable help.

## Changelog
Available [here](/deploy/changelog.txt).
