var exec = require('cordova/exec');

exports.lock = function (arg0, success, error) {
    exec(success, error, 'Controller', 'lock', [arg0]);
};

exports.unLock = function (arg0, success, error) {
    exec(success, error, 'Controller', 'unLock', [arg0]);
};
