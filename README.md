# CityHealthMap2.0
City Health Map upgrade to AWS serverless Application with DynamoDB, Lambda and S3 hosting

## Preparing
Firstly, We need an AWS Console Account. And add an IAM user with both 'Programmatic access' and 'AWS Management Console access' for daily operation, copy its access key(access key ID and access key secret). Rememeber to switch region to the one near you. 
Download AWS CLi. Set the access key with AWS configuration. Then your local computer can access AWS resource.

## DynamoDB to store data
Create Table in DynamoDB, and load data(testSample.csv) with `MigrationCSV2DynamoDB` into DynamoDB. If your env data are huge, you need to set your dynamoDB write capacity to speed up the data writing. After that you can set the write capacity with a small number. (SAM init a backend to provide a sample to create a permission for Lambda to access DynamoDB.)

## Lambda to serve backend
Download SAM. We can init, test, run and deploy our Lambda function locally with SAM. In this project, we use `sam deploy --guided` to deploy our `chm-backend-lambda` into AWS. SAM can get all your resource including API Gateway and Lambda ready for you. You can test the backend with API Gateway.

## S3 to host frontend
Build the `chm_frontend` with `npm run build` to produce files in /dist. Upload all files in /dist to S3 bucket (Set the bucket public accessable, and all the file can be publicly read). In `preporties` of bucket, enable `Static Website Hosting` to let S3 to host your frontend.

