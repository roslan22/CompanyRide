var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var userSchema = new Schema(
{
								//after user is verified the link to user profile is added
	userProfileId: 				{type: Schema.ObjectId, ref: 'userProfiles'},
	professionalEmail: 			{type: String, required: true, unique: true},
	firstName:					{type: String, required: true},
	lastName:					{type: String, required: true},
	companyName:				{type: String, required: true},
	occupation: 				{type: String, required: true},
	status :					{type: String, default: 'passive', enum: ['active','passive']},
	validationDate: 			{type: Date},
	lastLoginDate:				{type: Date, required: true},
	password:					{type: String, required: true},
  verificationPass: 			{type: String, required: true},					//for email verification
	deviceToken: 				    {type: String}					//for Google cloud messaging
});

module.exports = mongoose.model('users', userSchema, 'users'); //connects to users by userModel
