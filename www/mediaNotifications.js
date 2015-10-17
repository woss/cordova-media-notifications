var exec = require('cordova/exec');
var channel = require('cordova/channel');
module.exports = {
  channels: {},
  verbose: false,
  createEvent: function(type, data) {
      var event = document.createEvent('Event');
      event.initEvent(type, false, false);
      if (data) {
          for (var i in data) {
              if (data.hasOwnProperty(i)) {
                  event[i] = data[i];
              }
          }
      }
      return event;
  },
  addEventListener: function(eventName, cb) {
    if (!(eventName in this.channels)) {
      var $this = this;
      exec(function() {
        $this.channels[eventName] = channel.create(eventName);
        $this.channels[eventName].subscribe(cb);
      }, function(err) {
        console.log('ERROR with adding a listener : ' + eventName, err);
      }, "mediaNotifications", "addEventListener", [eventName]);
    } else {
      this.channel.subscribe(cb);
    }
  },
  removeEventListener: function(eventName, f) {
    if (eventName in this.channels) {
      var $this = this;
      exec(function() {
        $this._channels[eventName].unsubscribe(f);
      }, function(err) {
        console.log("ERROR removeEventListener: " + err)
      }, "mediaNotifications", "removeEventListener", [eventName]);
    }
  },

  /**
    Bind all events
  */
  bindAll: function(success, error) {
    var eventNames = [
      'com.spotify.music.playbackstatechanged',
      'com.spotify.music.metadatachanged',
      'com.spotify.music.queuechanged'
    ]
    eventNames.forEach(function(eventName) {
      exec(success, error, "mediaNotifications", "addEventListener", [eventName]);
    });
  },
  /**
  * Fires and event
  */
  fireEvent: function(type, data) {
    var event = this.createEvent( type, data );
     if (event && (event.type in this.channels)) {
         this.channels[event.type].fire(event);
     }
  },

  // Verbose log output

  log: function(type, data) {
    if (this.verbose === true) {
      console.log(type, data);
    }
  }
};
