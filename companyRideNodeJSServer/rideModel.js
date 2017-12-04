var mongoose = require('mongoose');
var enums = require('./enums');
var Schema = mongoose.Schema;

var rideSchema = new Schema(
{
	timeOffset:			{type: Number, default: 0},                		     //offset from UTC in milliseconds
	eventType: 			{type: String, enum: enums.eventTypes, required: true},
	driverRideReqId: 	{type: Schema.ObjectId, ref: 'rideRequests' }, 		 //reference to driver ride request
	driverProfileId: 	{type: Schema.ObjectId, ref: 'userProfiles' },		 //reference to driver user profile
	driverFullName: 	{type: String, required: true},
	driverOccupationTitle: {type: String, required: true},
	startDate: 			{type: Date, required: true},
	stopDate: 			{type: Date, required: true},
	status: 			{type: String, enum: enums.ridesStatuses, required: true},
	weekday: 			{type: Number, min: 1, max: 7, required: true},
	maxPickUpHour: 		{type: Number, min: 0, max: 23.99, default: 0},		//max pick up time among all hitchers
	minPickUpHour: 		{type: Number, min: 0, max: 23.99, default: 0},		//min pick up time among all hitchers
	maxNumOfHitchers: 	Number,
	from:
	{
		type: 			{type: String, default: 'Point', enum: ['Point']},
		address: 		{type: String, required: true },
		coordinates:
		{
			long: 		{type: Number, required: true},
			lat: 		{type: Number, required: true}
		}
	},
	to:
	{
		type: 			 {type: String, default: 'Point', enum: ['Point']},
		address:		 {type: String, required: true},
		coordinates:
		{
			long: 		 {type: Number, required: true},
			lat: 		 {type: Number, required: true}
		}
	},
	hitchers:
	[{
		timeOffset:				{type: Number, default: 0},
		userProfileId: 			{type: Schema.ObjectId, ref:'userProfiles'},
		hitcherRideReqId: 		{type: Schema.ObjectId, ref:'rideRequests'},
		fullName: 				{type: String, required: true},
		occupationTitle: 		{type: String, required: true},
		status: 				{type: String, required: true, enum: enums.ridesHitcherStatuses},
		pickUp:
		{
			time:				{type: Number,min: 0, max: 23.99, required: true},
			address: 			{type: String, required: true},
			coordinates:
			{
				long: 				{type: Number, required: true},
				lat: 				{type: Number, required: true}
			}
		},
		drop:
		{
			address: 			{type: String, required: true},
			coordinates:
			{
				long: 				{type: Number, required: true},
				lat: 				{type: Number, required: true}
			}
		},
		messages:
		{
			received:			[String],
			sent:				[String]
		}
	}]
});

rideSchema.index({"hitchers.userProfileId": 1});
rideSchema.index({"driverProfileId": 1});
rideSchema.index({"from.coordinates": '2dsphere'});

var rideModel = mongoose.model('rides', rideSchema, 'rides');

module.exports = rideModel;
