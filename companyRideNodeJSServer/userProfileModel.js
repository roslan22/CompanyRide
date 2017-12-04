var mongoose = require('mongoose');
var Schema = mongoose.Schema;
var enums = require('./enums');

var userProfileSchema = new Schema(
{
	fullName:				{type: String, required: true},  	//first name + last name
	occupationTitle:		{type: String, required: true},		//position + company
							//user that were actively blocked by user
	blockedUsers:			[{type: Schema.ObjectId, ref: 'userProfiles'}],
	numOfRidesAsDriver: 	{type: Number, min: 0, default: 0, required: true},
	numOfRidesAsHitcher: 	{type: Number, min: 0, default: 0, required: true},
	rating: 				{type: Number, min: 0, default: 0, required: true},
	//numbers of special requests for hitchers in case user is a driver, 1- "no smoking", 2 - "no eating" etc.
	specialRequests: 		[Number],
	badges:					[Number],
	messages:
	[{
		message:			String,
							//reference to a ride the message is related to
		rideId:				{type: Schema.ObjectId, ref: 'rides'},
		type: 				{type: String, enum: enums.messageTypes}
	}]
});

module.exports = mongoose.model('userProfiles', userProfileSchema, 'userProfiles');
