var config = require('./config');
var fs = require('fs');
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
  var cursor = collection.find({ name: name });

  var dir = name.replace(/[^a-zA-Z0-9\.]+/g, '-');
  var filename = name.split('?')
  filename = filename[filename.length - 1];

  if (!fs.existsSync(dir)){
      fs.mkdirSync(dir);
  }

    cursor.each(function (err, item) {
      if (!err && item) {
        if (item.create) {
          console.error('create')
          textData = item.create.data;
        }
          if (item.op) {
            textData = text.apply(textData, item.op);
            console.error(item.m.e);
            if (item.v >= from) {
              fs.writeFileSync(dir + '/' + filename + '_' + item.m.e.toISOString(), textData)
            }
          }
        } else {
           console.error(err);
        }
        if (item === null) {
          process.exit(0);
        }
      });

});
