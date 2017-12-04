var userModel = require('./userModel');
var generics = require('./genericFunctions');
var nodemailer = require("nodemailer");
var generalVars = require("./generalVars");
var userProfileModel = require('./userProfileModel');

var smtpTransport = nodemailer.createTransport("SMTP",{
    service: "Gmail",
    auth: {
        user: "noreply.companyride@gmail.com",
        pass: "!companyride2015?2016"
    }
});
var rand,mailOptions,host,link;
/*------------------SMTP Over-----------------------------*/

exports.getUserById = function(req, res)
{
	console.log("Fetching user " + req.params.id);

	var id = req.params.id;

	userModel.findById(id).select('-__v').exec(function callback(err, doc)
	{
		if(err)
		{
			console.log("Problem fetching user ", id);
			return res.status(500).send(generics.wrappedResponse("error", 'Problem fetching user: ' + err.message));
		}
        if (!doc)
        {
            return res.status(400).send(generics.wrappedResponse("error", 'User with id ' + id + " wasn't found"));
        }
		else
		{
			 return res.status(200).send(generics.wrappedResponse("success", "User successfully fetched",doc));
		}
	});
}


exports.createNewUser = function(req, res)
{
	var newCreateUserRequest = req.body;
	newCreateUserRequest.lastLoginDate = new Date();
   console.log(newCreateUserRequest);

	if(!newCreateUserRequest.firstName)
	{
        return res.status(400).send(generics.wrappedResponse("error","First name should be specified."));
	}
	if(!newCreateUserRequest.lastName)
	{
		return res.status(400).send(generics.wrappedResponse("error","Last name should be specified."));
	}
	if(!newCreateUserRequest.companyName)
	{
		return res.status(400).send(generics.wrappedResponse("error","Company should be specified."));
	}
	if(!newCreateUserRequest.occupation)
	{
		return res.status(400).send(generics.wrappedResponse("error","Ocupation should be specified."));
	}
	if(!newCreateUserRequest.password)
	{
		  return res.status(400).send(generics.wrappedResponse("error","No password."));
	}
	if(!newCreateUserRequest.professionalEmail)
	{
		return res.status(400).send(generics.wrappedResponse("error","No email."));
	}
	newCreateUserRequest.validationDate = new Date();
	newCreateUserRequest.verificationPass = Math.floor((Math.random() * 100000) + 54);
	//----------Create Model---------

	userModel.create(newCreateUserRequest, function(err,result)
	{
		if (err)
		{
			var response = new String("Error adding new user. " + err);
			if (!err.errors)
            {
            	console.log("ERROR: "+ response);
            	return res.status(400).send(generics.wrappedResponse("error","Error adding new user "));
			}
			else generics.getSchemaValidationErrors(err.errors, function (schemaErrorsString)
			{
				console.log("ERROR: "+ response+ schemaErrorsString);
				return res.status(400).send(generics.wrappedResponse("error","Error adding new user "));
			});
		}
		else if (!result)
		{
			console.log("ERROR: "+ response+ schemaErrorsString);
	           return res.status(500).send(generics.wrappedResponse("error",'An error has occurred while adding new user'));
        }
		else
        {
            console.log(result);
			//send email with verification to .idUser
			sendEmailToUser(req, result._id,result.verificationPass, result.professionalEmail, function(err)
            {
			    if (err)
                {
                    console.log("ERROR: failed to send email "+ err);
                }
                else
                {
                    return res.status(200).send(generics.wrappedResponse("success","Successfully added new user",{"userId":result._id}));
                }
            });
        }
	});
}

function sendEmailToUser(req, userId, rand, userEmail, callback)
{
    host=req.get('host');
    link="http://"+ generalVars.serverIp + "/verifyMail/" + userEmail + '/'+ userId +'/'+ rand; //enter donain
      console.log("link " + link);
    mailOptions={
        to : userEmail,
        subject : "Please confirm your Email account",
        html : "Hello,<br>Thank you for choosing companyRide, please verify your email: <br><a href="+link+">Verify!</a>"
    }
    console.log(mailOptions);
    smtpTransport.sendMail(mailOptions, function(error, response)
    {
        if(error)
        {
            console.log(error);
            callback("error");
         }
         else
         {
            console.log("Message sent: " + response.message);
            callback(null);
         }
     });
 }


exports.updateUserById = function(req, res)
{
	  console.log("In updateUserById");
    console.log("Received data: " + JSON.stringify(req.body));
	  var id = req.params.id;
    var newInfo = {};
    if (req.body.professionalEmail) newInfo.professionalEmail = req.body.professionalEmail;
    if (req.body.lastName) newInfo.lastName = req.body.lastName;
    if (req.body.firstName) newInfo.firstName = req.body.firstName;
    if (req.body.companyName) newInfo.companyName = req.body.companyName;
    if (req.body.occupation) newInfo.occupation = req.body.occupation;
    if (req.body.password && req.body.password != '') newInfo.password = req.body.password;
    console.log("Will update user with the following data: " + JSON.stringify(newInfo));
    if (newInfo.professionalEmail || newInfo.lastName || newInfo.firstName
    || newInfo.companyName || newInfo.occupation)
    {
      //first find the user
      userModel.findOne({_id:id}, function(err, doc)
      {
        if(err)
        {
          console.log("Error: " + err.message);
          return res.status(500).send(generics.wrappedResponse("error",'Problem fetching ride request: ' + id));
        }
        if (!doc)
        {
            return res.status(400).send(generics.wrappedResponse("error", 'User with id ' + id + " wasn't found"));
        }
        // if password match - update
        if(newInfo.password && newInfo.password == doc.password)
        {
          var verificationEmail = newInfo.professionalEmail;
          // if email was specified - just remember to send verification
          if( newInfo.professionalEmail )
            delete newInfo.professionalEmail;
        	userModel.findOneAndUpdate({_id: id}, {$set: newInfo},
            function(err, doc)
        	{
        		if(err)
        		{
              console.log("Error: " + err.message);
        			return res.status(500).send(generics.wrappedResponse("error",'Problem updating ride request: ' + id));
        		}
            if (!doc)
            {
                return res.status(400).send(generics.wrappedResponse("error", 'User with id ' + id + " wasn't found"));
            }
        		else
        		{
                    // if user was already verified - update info
                    if (doc.userProfileId)
                    {
                        var occTitle = "";
                        if (doc.occupation) occTitle = doc.occupation;
                        if (doc.companyName)
                          if(occTitle == "") occTitle = doc.companyName;
                          else occTitle = occTitle + " at " + doc.companyName;
                        var userProfileNewInfo =
                        {
                            fullName: doc.firstName + " " + doc.lastName,
                            occupationTitle : occTitle
                        };

                        userProfileModel.findOneAndUpdate({_id: doc.userProfileId}, {$set: userProfileNewInfo},
                        function(err, userProfdoc)
                    	{
                    		if(err)
                    		{
                          console.log("Error: " + err.message);
                    			return res.status(500).send(generics.wrappedResponse("error",'Problem updating user profile: ' + doc.userProfileId));
                    		}
                            if (!userProfdoc)
                            {
                                return res.status(400).send(generics.wrappedResponse("error", 'User with id ' + id + " wasn't found"));
                            }
                    		else
                    		{       // if the email was change - reinitiate the verification
                                if (verificationEmail)
                                {
                                    sendEmailToUser(req, doc._id, doc.verificationPass, verificationEmail,
                                    function(err)
                                    {
                                        if (err) return res.status(500).send(generics.wrappedResponse("error",'Failed sending email for verification'));
                                        else return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated. Verification email has been send", doc));
                                    });
                                }
                                else
                                  return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc));
                            }
                        });
        	        }
                  // user was not yet verified - resend verificatio email to the new email
                    else
                    {
                        if (verificationEmail)
                        {
                            sendEmailToUser(req, doc._id, doc.verificationPass, verificationEmail,
                            function(err)
                            {
                                if (err) return res.status(500).send(generics.wrappedResponse("error",'Failed sending email for verification'));
                                else return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated. Verification email has been send", doc));
                            });
                        }
                        else
                           return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc));
                    }
                }
            });
          }
          // no password was specified or passwords don't match
          else{
            return res.status(400).send(generics.wrappedResponse("error", "Not a valid password for update!"));
          }
      });
    }
    else
    {
        return res.status(400).send(generics.wrappedResponse("error", "No information to update received"));
    }
}

exports.updateUser = function(id, newInfo, callback)
{
    userModel.findOneAndUpdate({_id: id}, {$set: newInfo},
    function(err, doc)
    {
        if(err)
        {
            console.log("Problem fetching user " + id);
            callback("Problem fetching user " + id);
        }
        if (!doc)
        {
            callback('User with id ' + id + " wasn't found");
        }
        else
        {
            console.log("Successfully updated user " + id);
            callback(null, 'User id ' + id + " was successfully updated");
        }
    });
}

exports.setUserToken = function(req, res)
{
  var id = req.params.id;
  var token = req.params.token;
  console.log("Device token received: " + token + " for user: " + id);
  var newInfo = {
    "deviceToken" : token
  };
  userModel.findOneAndUpdate({_id: id}, {$set: newInfo},
  function(err, doc)
  {
      if(err)
      {
          console.log("Problem fetching user " + id);
          return res.status(400).send(generics.wrappedResponse("error", "Problem fetching user: " + id));
      }
      if (!doc)
      {
          return res.status(400).send(generics.wrappedResponse("error", "User was not found for id: " + id));
      }
      else
      {
          console.log("Successfully updated user " + id);
          return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc));
      }
  });
}
