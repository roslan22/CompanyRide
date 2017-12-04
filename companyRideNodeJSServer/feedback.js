var generalVars = require("./generalVars");
var userProfiles = require('./userProfiles');
var generics = require('./genericFunctions');
var userProfileModel = require('./userProfileModel');
var userModel = require('./userModel');
var users = require('./users');
var nodemailer = require("nodemailer");

var smtpTransport = nodemailer.createTransport("SMTP",{
    service: "Gmail",
    auth: {
        user: "noreply.companyride@gmail.com",
        pass: "!companyride2015?2016"
    }
});

exports.sendEmailToUser = function(req, res)
{
      var category = req.params.category;
      var userId = req.params.userId;
      var feedback = req.body.feedback;
      var userEmail = "";

      console.log("Start sending feedback from user with id  " + userId +
      " category : " + category + ", feedback : " + feedback);

      userModel.findById(userId).select("-__v").exec(
    	function callback(err, doc)
    	{
    		if(err)
    		{
                message = "Wrong user id: " + userId;
                return res.status(500).send(generics.wrappedHTMLResponse("error",' Error: '));
    		}
    		if (!doc)
    		{
                message = "Invalid user id: " + userId;
                return res.status(500).send(generics.wrappedHTMLResponse("error",' Error: '));
    		}
    		else
    		{
              userEmail = doc.professionalEmail;
              if(userEmail != "")
              {
                console.log("User email was found: " + userEmail);
                sendFeedback(req, res, userEmail, category, feedback, sendStatus)
              }
              else {
                return res.status(500).send(generics.wrappedHTMLResponse("error", "user email wasn't found"));
              }
       }
    	});
  };

  function sendStatus(err, res)
  {
      if (err) return res.status(500).send(generics.wrappedResponse("error",'Failed sending email for feedback'));
      else return res.status(200).send(generics.wrappedResponse("success","Feedback mail was sent", null));
  }

  function sendFeedback(req, res, userEmail, category, feedback, sendStatus)
  {
      host=req.get('host');
      userAllowsEmail = req.body.allowsEmail;
      userEmailString = "";
      if(userAllowsEmail === "YES"){
        userEmailString = "User allows sending reply to his/her email";
      }
      else {
        userEmailString = "User doesn't allow sending reply to his/her email";
      }
      mailOptions={
          to : "roslan22@gmail.com, vvvalera@gmail.com",
          subject : "User's feedback -" + category,
          html : "Hello,<br> CompanyRide's user with email: " +  userEmail + " sent us following feedback: <br>" + feedback + "<br><br>"
                 + userEmailString
      }
      console.log(mailOptions);
      smtpTransport.sendMail(mailOptions, function(error, response)
      {
          if(error)
          {
              console.log(error);
              sendStatus("error", res);
           }
           else
           {
              console.log("Message sent: " + response.message);
              sendStatus(null, res);
           }
       });
   }
