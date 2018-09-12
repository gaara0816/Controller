var exec = require('cordova/exec');

exports.lock = function (arg0, success, error) {
    exec(success, error, 'Controller', 'lock', [arg0]);
};

exports.pause = function (arg0, success, error) {
    exec(success, error, 'Controller', 'pause', [arg0]);
};

exports.unLock = function (arg0, success, error) {
    exec(success, error, 'Controller', 'unLock', [arg0]);
};

exports.update = function (arg0, success, error) {
    exec(success, error, 'Controller', 'update', [arg0]);
};

exports.changeKPadPower = function (arg0, success, error) {
    exec(success, error, 'Controller', 'changeKPadPower', [arg0]);
};

exports.changePPadSpeakerPower = function (arg0, success, error) {
    exec(success, error, 'Controller', 'changePPadSpeakerPower', [arg0]);
};
