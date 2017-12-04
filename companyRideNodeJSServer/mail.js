var generalVars = require("./generalVars");
var userProfiles = require('./userProfiles');
var generics = require('./genericFunctions');
var userProfileModel = require('./userProfileModel');
var userModel = require('./userModel');
var users = require('./users');

exports.verifyUser = function(req, res)
{
    var id = req.params.userId;
    var verification = req.params.verification;
    var mail = req.params.mail;
    var message = '';
    console.log("Start verify user with id: " + id + " for mail: " + mail);

    userModel.findById(id).select("-__v").exec(
  	function callback(err, doc)
  	{
  		if(err)
  		{
              message = "Wrong user id: " + id;
              return res.status(500).send(generics.wrappedHTMLResponse("error", message + ' Error: ' + err.message));
  		}
  		if (!doc)
  		{
              message = "Invalid user id: " + id;
              return res.status(500).send(generics.wrappedHTMLResponse("error", message + ' Error: ' + err.message));
  		}
  		else
  		{
              if (verification == doc.verificationPass)
              {
                  if(doc.status === "passive")
                  {
                       var userProfileNewDoc = new userProfileModel(
                       {
                           fullName: doc.firstName + " " + doc.lastName,
                           occupationTitle : doc.occupation + " at " + doc.companyName,
                           messages:
                           [
                               {
                                   message:"Welcome to CompanyRide Application. Enter your ride habits, wait to be matched with a companion and enjoy a ride together!",
                                   type: "notification"
                               }
                           ]
                       });

                       userProfiles.createNewUserProfile ( userProfileNewDoc,
                       function(err, doc)
                       {
                           if (err)
                           {
                             message = "Error adding new user profile, try again!";
                             return res.status(500).send(generics.wrappedHTMLResponse("error", message + ' Error: ' + err.message));
                           }
                            else
                            {
                                users.updateUser(id, {
                                  "userProfileId" : doc._id,
                                  "professionalEmail" : mail,
                                  "status"        : "active"
                                },
                                function(err)
                                {
                                    if (err)
                                    {
                                      message = "Error adding user profile id, try again!";
                                      return res.status(500).send(generics.wrappedHTMLResponse("error", message + ' Error: ' + err.message));
                                    }
                                    else
                                    {
                                        console.log("Successfully added new user profile");
                                        userProfileModel.findOneAndUpdate({_id: doc._id}, {$push:{"blockedUsers": doc._id}},
                                        function (err, doc)
                                        {
                                            if (err || !doc)
                                            {
                                              message = "Error adding user profile id to self blocked list, try again!";
                                              return res.status(500).send(generics.wrappedHTMLResponse("error", message + ' Error: ' + err.message));
                                            }
                                            else
                                            {
                                              message = "Email was successfully verified, you can go back and use our App!";
                                              return res.status(200).send(generics.wrappedHTMLResponse("info", message));
                                            }
                                        });
                                    }
                                });
                           }
                      });
                  }
                  // active user
                  else
                  {
                    // first update the mail for the user
                    users.updateUser(id, {
                      "professionalEmail" : mail
                    },
                    function(err)
                    {
                        if (err)
                        {
                          return res.status(500).send(generics.wrappedHTMLResponse("error", "Error updating email!",err.message));
                        }
                    });
                    message = "Email was successfully verified, you can go back and use our App!";
                    return res.status(200).send(generics.wrappedHTMLResponse("info", message));
                  }
              }
              else
              {
                message = "Verification has failed, wrong verification code!";
                return res.status(400).send(generics.wrappedHTMLResponse("error", message));
              }
          }
  	});
}
