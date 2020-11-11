// Create clients and set shared const values outside of the handler.

// Get the DynamoDB table name from environment variables
const tableName = process.env.SAMPLE_TABLE;

// Create a DocumentClient that represents the query to add an item
const dynamodb = require('aws-sdk/clients/dynamodb');
const docClient = new dynamodb.DocumentClient();

/**
 * A simple example includes a HTTP get method to get one item by id from a DynamoDB table.
 */
exports.getAllEnvDataHandler = async (event) => {
  if (event.httpMethod !== 'GET') {
    throw new Error(`getMethod only accept GET method, you tried: ${event.httpMethod}`);
  }
  // All log statements are written to CloudWatch
  console.info('received:', event);
  let requestParams = event.queryStringParameters;
  if(requestParams.start_time == null){
    throw new Error(`Required param "start_time" is NULL`);
  }
  if(requestParams.end_time == null){
    throw new Error(`Required param "end_time" is NULL`);
  }
  let params = {
    TableName: tableName,
    FilterExpression:'#startTime BETWEEN :start_time AND :end_time',
    ExpressionAttributeNames: {
        '#startTime':'startTime'
    },
    ExpressionAttributeValues:{
        ':start_time':requestParams.start_time,
        ':end_time':requestParams.end_time
    },
    ProjectionExpression:["userId","locations","lineMark","pm25"]
  };
  if(requestParams.last_key_time != null && requestParams.last_key_user != null) {
    let lastKey = {
      startTime:requestParams.last_key_time,
      userId:Number.parseInt(requestParams.last_key_user)
    }
    params.ExclusiveStartKey = lastKey;
  }
  console.log(JSON.stringify(params));
  const data = await docClient.scan(params).promise();
  const response = {
      "statusCode":200,
      "headers": {
        "Access-Control-Allow-Headers" : "Content-Type",
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "OPTIONS,POST,GET"
      },
      "body":JSON.stringify(data),
      "isBase64Encoded": false
  };
  //test empty response
  // const response = {
  //       "statusCode":200,
  //       "headers":{
  //           "my_header": "my_value"
  //       },
  //       "body":{},
  //       "isBase64Encoded": false
  //   };
  return response;
}
