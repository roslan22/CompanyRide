var mongo = require('mongodb');
var mongoose = require('mongoose');

var userProfileModel = require('./userProfileModel');
var userModel = require('./userModel');

var generalVars = require('./generalVars');

mongoose.set('debug', true);

var mongoose_options = {};
mongoose_options.server = {};
mongoose_options.server.socketOptions = { keepAlive: 1 };
mongoose_options.replset = {};
mongoose_options.replset.socketOptions = { keepAlive: 1 };

var USERNAME = process.env.mongodb_user || "";
var PASSWORD = process.env.mongodb_pass || "";

var mongojson =
{
    "hostname":"localhost",
    "port":27017,
    "username":USERNAME,
    "password":PASSWORD,
    "name":"",
    "db":"CompanyRide"
}

var generate_mongo_url = function(obj)
{
  obj.hostname = (obj.hostname || 'localhost');
  obj.port = (obj.port || 27017);
  obj.db = (obj.db || 'test');

  if(obj.username && obj.password)
  {
    return "mongodb://" + obj.username + ":" + obj.password + "@" + obj.hostname + ":" + obj.port + "/" + obj.db;
  }
  else
  {
    return "mongodb://" + obj.hostname + ":" + obj.port + "/" + obj.db;
  }
}

var mongourl = generate_mongo_url(mongojson);

mongoose.connect(mongourl, mongoose_options);

var connection = mongoose.connection;

connection.on('error', console.error.bind(console, 'connection error:'));

var db = connection.db; // This is the native MongoDB interface, can also use modelName.collection

connection.once('open', function callback () {
  console.log('Mongoose connection established');
  mongo_connection(connection.db);
});

connection.on('close', function callback () {
  console.log('Mongoose connection closed');
});

var mongo_connection = function(db)
{
	console.log('In mongo_connection test' + db);
/*
	db.collection('userProfiles', {strict:true}, function(err, collection)
	{
		// if (err)
		// {
			console.log("The 'userProfiles' collection doesn't exist. Creating it with sample data...");
			populateUserProfiles();
		// }
	});

	db.collection('users', {strict:true}, function(err, collection)
	{
		// if (err)
		// {
			console.log("The 'users' collection doesn't exist. Creating it with sample data...");
			populateUsers();
		// }
	});
  */
  console.log('In mongo_connection test end');
};

//we using this explicit ObjectID for creating references between user and userProfiles documents
var ObjectId_1 = mongo.ObjectID("53f45f26780bfa040b36dfa3"); //userProfile docId of lucy Miller
var ObjectId_2 = mongo.ObjectID("53f45f26780bfa040b36dfa5"); //userProfile docId of Sam Williams
var ObjectId_3 = mongo.ObjectID("53f45f26780bfa040b36dfa7"); //userProfile docId of Michael Taylor
var ObjectId_4 = mongo.ObjectID("53f45f26780bfa040b36dfa9"); //userProfile docId of Emma Andersons

var populateUserProfiles = function()
{
	var userProfile_1 = new userProfileModel(
	{
		_id: ObjectId_1,
		fullName: "Lucy Miller",
        occupationTitle: "Software Developer at Google",
		blockedUsers: [ObjectId_1],
		numOfRidesAsDriver: 0,
		numOfRidesAsHitcher: 0,
		rating: 0,
		specialRequests:[1,2,4,3],
		messages:
		[]
	});

	var userProfile_2 = new userProfileModel(
	{
		_id: ObjectId_2,
		fullName: "Sam Williams",
        occupationTitle: "Computer Systems Analyst at Yahoo",
		blockedUsers: [ObjectId_2],
		numOfRidesAsDriver: 0,
		numOfRidesAsHitcher: 0,
		rating: 0,
		specialRequests:[1,2],
		messages: []
	});

    var userProfile_3 = new userProfileModel(
	{
		_id: ObjectId_3,
		fullName: "Michael Taylor",
        occupationTitle: "Market Research Analyst at Amazon",
		blockedUsers: [ObjectId_3],
		numOfRidesAsDriver: 0,
		numOfRidesAsHitcher: 1,
		rating: 20,
		specialRequests:[3,4],
		messages: [],
        badges:[1]
	});

    var userProfile_4 = new userProfileModel(
	{
		_id: ObjectId_4,
		fullName: "Emma Andersons",
        occupationTitle: "IT Manager at American Express",
		blockedUsers: [ObjectId_4, ObjectId_2],
		numOfRidesAsDriver: 4,
		numOfRidesAsHitcher: 1,
		rating: 120,
		specialRequests:[1,3],
		messages: [],
        badges:[1,2,5]
	});

    userProfile_1.save(function (err) {
        if (err) console.log('Error inserting userProfile_1 to DB: ' + err.toString());
    });

    userProfile_2.save(function (err) {
        if (err) console.log('Error inserting userProfile_2 to DB: ' + err.toString());
    });

    userProfile_3.save(function (err) {
        if (err) console.log('Error inserting userProfile_3 to DB: ' + err.toString());
    });

    userProfile_4.save(function (err) {
        if (err) console.log('Error inserting userProfile_4 to DB: ' + err.toString());
    });

}

var populateUsers = function()
{
	var user_1 = new userModel(
	{
		userProfileId: ObjectId_1,
		professionalEmail:"lucy.miller@google.com",
		firstName: "Vlada",
		lastName: "Leykin",
		companyName: "Google",
		occupation: "Software Developer",
		status :'active',
		validationDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		lastLoginDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		password:"123456",
		verificationPass:"12345"
	});

	var user_2 = new userModel(
	{
		userProfileId: ObjectId_2,
		professionalEmail: "sam.williams@yahoo.com",
		firstName: "Sam",
		lastName: "Williams",
		status :'active',
		companyName: "Yahoo",
		occupation: "Computer Systems Analyst",
		validationDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		lastLoginDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		password:"123456",
		verificationPass:"12345"
	});

    var user_3 = new userModel(
	{
		userProfileId: ObjectId_3,
		professionalEmail: "michael.taylor@amazon.com",
		firstName: "Michael",
		lastName: "Taylor",
		status :'active',
		companyName: "Amazon",
		occupation: "Market Research Analyst",
		validationDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		lastLoginDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		password:"123456",
		verificationPass:"12345"
	});

    var user_4 = new userModel(
	{
		userProfileId: ObjectId_4,
		professionalEmail: "emma.anderson@amexpress.com",
		firstName: "Emma",
		lastName: "Anderson",
		status :'active',
		companyName: "American Express",
		occupation: "IT Manager",
		validationDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		lastLoginDate: "Mon Aug 18 2014 13:41:47 GMT+0300 (Jerusalem Daylight Time)",
		password:"123456",
		verificationPass:"12345"
	});

	user_1.save(function (err) {
		if (err) console.log('Error inserting user_1 to DB: ' + err.toString());
	});

	user_2.save(function (err) {
		if (err) console.log('Error inserting user_2 to DB: ' + err.toString());
	});

    user_3.save(function (err) {
		if (err) console.log('Error inserting user_3 to DB: ' + err.toString());
	});

    user_4.save(function (err) {
        if (err) console.log('Error inserting user_4 to DB: ' + err.toString());
    });
}
