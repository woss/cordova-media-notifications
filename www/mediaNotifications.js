var exec = require('cordova/exec');

exports.bind = function(softwareId, success, error) {
    exec(success, error, "musicSoftwareBroadcastReceiver", "bind", [softwareId]);
};
exports.bindAll = function(success,error){
    var softwareIds = [
       'com.spotify.music.playbackstatechanged',
       'com.spotify.music.metadatachanged',
       'com.spotify.music.queuechanged'
    ]
    softwareIds.forEach(function(softwareId) {
        exec(success, error, "musicSoftwareBroadcastReceiver", "bind", [softwareId]);
    });
};
