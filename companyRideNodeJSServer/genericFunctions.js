var genVars  = require('./generalVars');
var request = require("request");
var millisecondsInDay = 1000 * 60 * 60 * 24;

exports.getSchemaValidationErrors = function(errors, callback)
{
	var errorStr = "";
	for (field in errors)
	{
		errorStr += errors[field].message;
	}
	callback(errorStr);
}

exports.wrappedResponse = function(status, message, data)
{
	console.log(message);
	wrapedObj = {};
	wrapedObj.status = status;
	wrapedObj.message = message;
	if (data)
	{
		wrapedObj.data = data;
	}

	return wrapedObj;
}

exports.wrappedHTMLResponse = function(status, message, errMsg)
{
	console.log(status + ": " +message);
	console.log(errMsg);
	return "<b1>"+ status + ": " + message + "</b1>";
}

exports.checkMonthWithinTimeRange = function(startDate,endDate, year, month)
{
	var monthInTheRange = false;
	if (startDate.getFullYear() < year && endDate.getFullYear() > year) monthInTheRange = true;
	else if (startDate.getFullYear() === year && endDate.getFullYear() > year && startDate.getMonth() <= month - 1 ) monthInTheRange = true;
	else if (startDate.getFullYear() < year && endDate.getFullYear() === year && endDate.getMonth() >= month - 1) monthInTheRange = true;
	else if (startDate.getFullYear() === year && endDate.getFullYear() === year &&
		startDate.getMonth() <= month - 1  && endDate.getMonth() >= month - 1) monthInTheRange = true;

	return monthInTheRange;
}


exports.getHourAsNumberFromDate = function(date)
{
	return (date.getHours() + (date.getMinutes() / 60));
}

exports.getUTCNewDate = function()
{
	var now = new Date();
	return new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(),  now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds());
}

exports.getUTCDateFromDate = function(date)
{
	return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
}

exports.getUTCNewDateFromMilliseconds = function(milliseconds)
{
	var date = new Date(milliseconds);
	return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(),  date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds());
}

exports.updateDateWithOffset = function(date, offset){
	return new Date(date.getTime() - offset);
}

exports.datePlusDay = function(date)
{
  //return exports.getUTCNewDateFromMilliseconds(date.getTime() + millisecondsInDay);
	return new Date(date.getTime() + millisecondsInDay);
}

exports.dateMinusDay = function(date)
{
  //return exports.getUTCNewDateFromMilliseconds(date.getTime() - millisecondsInDay);
	return new Date(date.getTime() - millisecondsInDay);
}

//weekday is 1-7
exports.getFirstWeekDayDateAfterDate = function(dateFrom, weekday)
{
    var nextDate = exports.datePlusDay(dateFrom);
		console.log("Weekday: " + weekday + " next date: " + nextDate );
    while (nextDate.getDay() + 1 != weekday)
    {
        nextDate = exports.datePlusDay(nextDate);
    }
    console.log("nextDate : " + nextDate);

    return nextDate;
}

exports.sendNotificationToUserWithMessage = function(hitcherId, rideId, message, messageType, callback)
{
  var headers = {
        'Authorization' : "key=" + genVars.gcmAPIKey,
        'Content-Type'  : 'application/json'
  };

  var gcmRequest = {
    'url'   :  genVars.gcmServerFullPath,
    'headers'   : headers,
    'json'  :  {
      'to'    :  "/topics/" + hitcherId,
      'data'  :  {
        'msgType' : messageType,
        'rideId'  : rideId,
        'message' : message
      },
    }
  };

  console.log("Will try to send notification to user: " + hitcherId + " request: " + JSON.stringify(gcmRequest));
  request.post(gcmRequest, function(err, res, body)
  {
        console.log("Response: " + res.statusCode + ", Body: " + JSON.stringify(body) + ", error: " + err);
        if (err){
          callback(err);
          return;
        }
        callback(null);
    });
   console.log("Notification message send try done!");
}
