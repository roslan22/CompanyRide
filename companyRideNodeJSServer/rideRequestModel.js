var mongoose = require('mongoose');
var enums = require('./enums');
var genericFunctions  = require('./genericFunctions');
var Schema = mongoose.Schema;

function dateInPastValidator(date)
{
	//86400000 millisecondsInDay
	var yesturday = new Date(genericFunctions.getUTCNewDate().getTime() - 86400000);
	console.log("yesturday new date " + yesturday);
	yesturday.setHours(0,0,0,0);
	console.log("Comparing two dates: " + yesturday + " >? and " + date);
	if (yesturday > date) return false;
}


var rideRequestSchema = new Schema(
{
	timeOffset:			{type: Number, default: 0},
	userProfileId: 		{type: Schema.ObjectId, ref: 'userProfiles'},
	rideType: 			{type: String, enum: enums.rideTypes, required: true},
    eventType: 			{type: String, enum: enums.eventTypes, required: true},
	inconvenientUsers: 	[{type: Schema.ObjectId, ref: 'userProfiles'}],
	blockedUsers: 		[{type: Schema.ObjectId, ref: 'userProfiles'}],
	creationDate: 		{type: Date, required: true, default:Date.now},
	startDate: 			{type: Date, required: true, validate: [dateInPastValidator,'Start date is in past!']},
	stopDate:  			{type: Date, required: true, validate: [dateInPastValidator,'Stop date is in past!']},
	weekday: 			{type: Number, min: 1, max: 7, required: true},
	preferredRideTime:
	{
			fromHour: {type: Number, min: 0, max: 23.99, required: true},
			toHour: {type: Number, min: 0, max: 23.99, required: true}
	},
	maxNumOfHitchers: 	Number,
	radius: 			Number,
	status: 			{type: String, enum: enums.rideRequestsStatuses, required: true},
	from:
	{
		type: 			{type: String, default: 'Point', enum: ['Point']},
		address: 		{type: String, required: true},
		coordinates:
		{
			long: 		{type: Number, required: true},
			lat: 		{type: Number, required: true}
		}
	},
	to:
	{
		type: 			{type: String, default: 'Point', enum: ['Point']},
		address: 		{type: String, required: true},
		coordinates:
		{
			long: 		{type: Number, required: true},
			lat: 		{type: Number, required: true}
		}
	}
});

rideRequestSchema.index({"userProfileId": 1});
rideRequestSchema.index({"to.coordinates": '2dsphere'});
rideRequestSchema.index({"from.coordinates": '2dsphere'});

module.exports = mongoose.model('rideRequests', rideRequestSchema, 'rideRequests');
