cordova.define("com.hrj.childfolioplugin.TakeVideo", function(require, exports, module) {
var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'TakeVideo', 'openVideo', [arg0]);
};

});
