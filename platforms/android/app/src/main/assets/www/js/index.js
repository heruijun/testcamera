var app = {

    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    receivedEvent: function(id) {
        document.getElementById('picSelect').addEventListener('click', function() {
           Cordova.exec(function(r) {
               var div = document.getElementById('picResult');
               var result = JSON.parse(r);
               div.innerHTML = result;
           }, null, "ChildFolioPlugin", "openCamera", [9]);
        }, false);

        document.getElementById('videoSelect').addEventListener('click', function() {
           Cordova.exec(function(r) {
               var div = document.getElementById('videoResult');
               div.innerHTML = r;
           }, null, "TakeVideo", "openVideo", []);
        }, false);
    }
};

app.initialize();