// Create clients and set shared const values outside of the handler.

// Get the DynamoDB table name from environment variables
const tableName = process.env.DYNAMODB_TABLE;//读的时 template.yml而非 env.json 中的环境变量

// Create a DocumentClient that represents the query to add an item
const dynamodb = require('aws-sdk/clients/dynamodb');
const docClient = new dynamodb.DocumentClient();

/**
 * A simple example includes a HTTP get method to get all items from a DynamoDB table.
 */
exports.getEnvDataHandler = async (event) => {
    if (event.httpMethod !== 'GET') {
        throw new Error(`getAllItems only accept GET method, you tried: ${event.httpMethod}`);
    }
    // All log statements are written to CloudWatch
    console.info('received:', event);
    let requestParams = event.queryStringParameters;
    if(requestParams.user_id == null){
        throw new Error(`Required param "user_id" is NULL`);
    }
    if(requestParams.start_time == null){
        throw new Error(`Required param "start_time" is NULL`);
    }
    if(requestParams.end_time == null){
        throw new Error(`Required param "end_time" is NULL`);
    }
    let userId = Number.parseInt(requestParams.user_id);
    let params = {
        TableName: tableName,
        KeyConditionExpression:'#userId = :hkey and #startTime BETWEEN :start_time AND :end_time',
        ExpressionAttributeNames: {
            '#userId':'userId',
            '#startTime':'startTime'
        },
        ExpressionAttributeValues:{
            ':hkey':userId,
            ':start_time':requestParams.start_time,
            ':end_time':requestParams.end_time
        }
    };
    // console.dir(params);
    const data = await docClient.query(params).promise();
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
    //query test--------------
    // // get all items from the table (only first 1MB data, you can use `LastEvaluatedKey` to get the rest of data)
    // // https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/DynamoDB/DocumentClient.html#scan-property
    // // https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Scan.html
    // var params = {
    //     TableName : tableName,
    //     KeyConditionExpression:'userId = :hkey and startTime < :rkey',
    //     ExpressionAttributeValues:{
    //         ':hkey':19,
    //         ':rkey':'2018-01-09'
    //     }
    // };
    // const data = await docClient.query(params).promise();
    //scan test-----------------
    // var params = {
    //     TableName : tableName
    // };
    // const data = await docClient.scan(params).promise();

    // const items = data.Items;

    // const response = {
    //     statusCode: 200,
    //     body: JSON.stringify(items)
    // };

    // // // All log statements are written to CloudWatch
    // console.info(`response from: ${event.path} statusCode: ${response.statusCode} body: ${response.body}`);
    // return response;
    //--------------
    //API GateWay acceptable response
    // var responseBody = {
    //     "key3": "value3",
    //     "key2": "value2",
    //     "key1": "value1"
    // };

    // var response = {
    //     "statusCode":200,
    //     "headers":{
    //         "my_header": "my_value"
    //     },
    //     "body":JSON.stringify(responseBody),
    //     "isBase64Encoded": false
    // };
    return response;

}
