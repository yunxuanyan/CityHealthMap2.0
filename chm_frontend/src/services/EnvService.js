/* eslint-disable no-console */
import * as $ from 'jquery';

// @ts-ignore
// const config = require("../config.json");

export default class EnvService {

    defaultFields = ['id','date','latitude','longitude'];

    // let host = config.backend_host_ip;
    // let port = config.backend_host_port;
    //prod
    // backend = window.location.origin.replace("8080","8000");
    //dev
    backend = "https://7d10gg90p6.execute-api.ap-southeast-2.amazonaws.com/Prod";

    getUserEnv=(userId,startTime,endTime)=>{

        return new Promise((resolve,reject)=>{
            $.ajax({
                type:"GET",
                url:`${this.backend}/getEnvData/?user_id=${userId}&start_time=${startTime}&end_time=${endTime}`,
                success:(res)=>{
                    resolve(res);
                },
                error:(err)=>{
                    reject(err);
                },
            });
        });
    }

    getAllUserEnv=(lastkey)=>{
        let key='';
        if(lastkey !=null){
            key = `&last_key_time=${lastkey.startTime}&last_key_user=${lastkey.userId}`;
        }
        let url=`${this.backend}/getAllEnvData?start_time=2018-01-08%2000:00:00&end_time=2019-11-21%2000:00:00${key}`
        return new Promise((resolve,reject)=>{
            $.ajax({
                type:"GET",
                url:url,
                success:(res)=>{
                    resolve(res);
                },
                error:(err)=>{
                    reject(err);
                },
            });
        });        
    }

    
}