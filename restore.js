var config = require('./config');

var text = require('ottypes').text;

var MongoClient = require('mongodb').MongoClient;

var name = process.argv[2];
var from = process.argv[3] || 0;

MongoClient.connect(config.database.connection, function (err, db) {
    if (err || !db) {
        console.error('Connect to mongo failed: ', err);
        process.exit(1);
        return;
    }

    var collection = db.collection('codio_ops');

    var textData = '';

    var cursor = collection.find({name: name});
      cursor.each(function(err, item) {
      	if (!err && item) {
      		if (item.op) {
      			textData = text.apply(textData, item.op);
      			if (item.v >= from) {
	      			console.log(textData);
	      			console.log('#####################################################################');
	      		}
      		}
      	}
      	if (item === null) {
      		process.exit(0);
      	}
      });

});