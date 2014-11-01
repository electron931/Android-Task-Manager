#%Крутое название%
 =======

 ## Как запустить:

 Из папки проекта:

* `npm install`  -- установит все зависимости из package.json
* `bower install` -- установит все зависимости из bower.json
* `npm run make` -- запустит сборщик gulp
* `node server.js` -- запустит сервер (http и websocket), или, если нужен автоматический
перезапуск сервера после изменения файлов проекта: (предварительно устанавливаем)
`npm install -g supervisor` 
и запускаем
`supervisor server.js`
