var exec = require('cordova/exec');

exports.initialize = function (success, error) {
    exec(success, error, 'Controller', 'initialize', []);
};

exports.lock = function (success, error) {
    exec(success, error, 'Controller', 'lock', []);
};

exports.pause = function (success, error) {
    exec(success, error, 'Controller', 'pause', []);
};

exports.unLock = function (success, error) {
    exec(success, error, 'Controller', 'unLock', []);
};

exports.power = function (success, error) {
    exec(success, error, 'Controller', 'power', []);
};

exports.gotoSettings = function () {
    exec(undefined, undefined, 'Controller', 'gotoSettings', []);
};

exports.offLineLock = function (success, error) {
    exec(success, error, 'Controller', 'offLineLock', []);
};

exports.stmStatus = function (success, error) {
    exec(success, error, 'Controller', 'stmStatus', []);
};

exports.callJSInit = function () {
    cordova.require('cordova/channel').onCordovaReady.subscribe(function () {
        exec(succeedCallback, null, "Controller", "callJSInit", []);
        function succeedCallback(message) {
            //执行js代码
            eval(message);
        }
    });
}
