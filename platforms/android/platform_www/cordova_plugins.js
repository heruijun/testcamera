cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "com.hrj.childfolioplugin.ChildFolioPlugin",
    "file": "plugins/com.hrj.childfolioplugin/www/ChildFolioPlugin.js",
    "pluginId": "com.hrj.childfolioplugin",
    "clobbers": [
      "cordova.plugins.ChildFolioPlugin"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-whitelist": "1.3.3",
  "com.hrj.childfolioplugin": "1.0.0"
};
// BOTTOM OF METADATA
});