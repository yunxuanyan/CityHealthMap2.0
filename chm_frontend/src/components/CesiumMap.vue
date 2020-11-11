<template>
    <div>
        <div id="cesiumContainer" class="viewer">
            <OptionTable class="cesium-button toolbar-left" v-on:submitchoice="loadChoice"></OptionTable>
            <AboutUs class="cesium-button about-us"></AboutUs>
            <PMLegend class="cesium-button legend" v-if="IsPMLegendShown"></PMLegend>
            <TemperatureLegend class="cesium-button legend" v-if="IsTemperatureShown"></TemperatureLegend>
            <HumidityLegend class="cesium-button legend" v-if="IsHumidityShown"></HumidityLegend>
        </div>
        <!-- <div id="animationContainer"></div> -->
    </div>
</template>

<script>
import http from 'vue-resource';
import Vue from 'vue';
import OptionTable from './OptionTable.vue';
import AboutUs from './AboutUs.vue';
import PMLegend from './PMLegend.vue';
import TemperatureLegend from './TemperatureLegend.vue';
import HumidityLegend from './HumidityLegend.vue';
import EnvService from '../services/EnvService';
Vue.use(http);

let viewerEntities = null;
let viewer = null;
export default {
    name: 'CesiumMap',
    data:function(){
        return {
            volunteerId:null,
            term:"1",
            field:"pm25",
            envData:null,
            envService: new EnvService(),
            timeouts:null,
            IsPMLegendShown:false,
            IsTemperatureShown:false,
            IsHumidityShown:false,
            time:{
                "1":{
                    startTime:"2018-01-07 12:00:00",
                    endTime:"2018-01-20 11:59:50"
                },
                "2":{
                    startTime:"2018-03-26 00:09:55",
                    endTime:"2018-04-03 00:00:00"                    
                },
                "3":{
                    startTime:"2018-06-23 12:00:55",
                    endTime:"2018-07-02 11:59:50"
                },
                "4":{
                    startTime:"2018-10-27 09:00:00",
                    endTime:"2018-11-07 11:59:10"
                }
            }
        }
    },
    components: {
        OptionTable,
        AboutUs,
        PMLegend,
        TemperatureLegend,
        HumidityLegend
    },
    mounted() {
        let Cesium = window.cesium;
        //YOUR_CESIUM_TOKEN can be got from https://cesium.com/ion/signin/tokens
        Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI0ODQ1MGEwOS1mMDk4LTQ4NDMtYTRkYi01Mjc1NDI3MWQ2ZGMiLCJpZCI6ODkxNiwic2NvcGVzIjpbImFzciIsImdjIl0sImlhdCI6MTU1MzA1NDMzMH0.Fndg4WDUnaD1rXyfNvsi3UDeJFQ7g9dR5U_owEhpLRw';
        viewer = new Cesium.Viewer('cesiumContainer',{
            sceneModePicker:false,
            homeButton:false,
            baseLayerPicker:false,
            geoCoder:false,
            navigationHelpButton:false,
            animation:false,
            timeline:false
        });
        // viewer.timeline.zoomTo(Cesium.JulianDate.fromIso8601("2018-01-12T00:00:00Z"),Cesium.JulianDate.fromIso8601("2019-01-01T00:00:00Z"));
        viewerEntities = viewer.entities;
        viewer.imageryLayers.remove(viewer.imageryLayers.get(0));
        viewer.imageryLayers.addImageryProvider(new Cesium.BingMapsImageryProvider({
            url : 'https://dev.virtualearth.net',
            key : 'AoP-LhJzZplGyzyekYwMdZ_U8V_J-6Cr2fRvOoQjU3lbNMApYha5J9P6D-7Kc-eY',//YOUR_BING_KEY
            mapStyle : Cesium.BingMapsStyle.CANVAS_DARK
        })); //Cesium.BingMapsStyle.CANVAS_GRAY  Cesium.BingMapsStyle.CANVAS_LIGHT Cesium.BingMapsStyle.CANVAS_DARK
        let camera = viewer.camera;
        camera.setView({
            destination: Cesium.Cartesian3.fromDegrees(116.3974, 39.9342, 100000) //北京
            // destination: Cesium.Cartesian3.fromDegrees(120.63, 31.3, 20000) //苏州
        });

        this.timeouts = []

        //如果已选择志愿者，则停止加载
        if(this.volunteerId != null){
            return;
        }
        this.loadAllEnvStep(null,0);
    },
    methods:{
        loadAllEnvStep(lastkey,count){
            if(count == 20) {
                return;
            }
            return this.envService.getAllUserEnv(lastkey).then(res=>{
                if(this.volunteerId != null){
                    return;
                }
                if(res.LastEvaluatedKey != null) {
                    this.loadAllEnvStep(res.LastEvaluatedKey,count+1);
                }
                this.loadDataIntoLinesFromArray(res.Items); //加载线条
            });
        },
        loadChoice(choice) {
            console.log(choice);

            // this.volunteerId = choice.volunteerId;
            this.field = choice.field;

            //管理图例及颜色
            if(this.field.includes('pm')){
                this.IsPMLegendShown = true;
                this.IsTemperatureShown = false;
                this.IsHumidityShown = false;
            } else if(this.field == 'temperature'){
                this.IsPMLegendShown = false;
                this.IsTemperatureShown = true;
                this.IsHumidityShown = false;
            } else if(this.field == 'humidity'){
                this.IsPMLegendShown = false;
                this.IsTemperatureShown = false;
                this.IsHumidityShown = true;
            } else {
                this.IsPMLegendShown = false;
                this.IsTemperatureShown = false;
                this.IsHumidityShown = false;
                return;
            }

            this.removeSpots();
            if(choice.volunteerId == this.volunteerId && choice.term == this.term){
                this.loadDataFromArray(this.envData);
            } else {
                this.volunteerId = choice.volunteerId;
                this.term = choice.term;
                this.envService.getUserEnv(this.volunteerId,this.time[this.term].startTime,this.time[this.term].endTime).then(res=>{
                    console.log('get results:'+res.Count);
                    this.envData = res.Items;
                    if(res.Count == null || res.Count == 0) {
                        alert("The volunteer is not involved to collect data in this season.");
                        return;
                    }
                    if(viewer != null){
                        let firstlocation = JSON.parse(this.envData[0].locations);
                        let lon = firstlocation[0].longitude;
                        let lat = firstlocation[0].latitude;
                        viewer.camera.setView({
                            destination: Cesium.Cartesian3.fromDegrees(lon, lat-0.12, 8000),  
                            orientation:{
                                pitch : Cesium.Math.toRadians(-30), 
                            }
                        })
                    }
                    this.removeSpots();
                    this.loadDataFromArray(this.envData);
                }).catch(err=>{
                    console.error(err);
                });
            }
            
        },
        getPMColor(val,Cesium){
            if(val<=12){
                return Cesium.Color.GREEN;
            } else if(val<=35){
                return Cesium.Color.YELLOW;
            } else if(val<=55){
                return Cesium.Color.ORANGERED;
            } else if(val<=150){
                return Cesium.Color.RED;
            } else if(val<=250){
                return Cesium.Color.PURPLE;
            } else {
                return Cesium.Color.MAROON;
            }
        },
        getPMColorType(val) {
            let pmThresholds = [12,35,55,150,250];
            return this.getColorType(val,pmThresholds);
        },
        getTemperatureColorType(val){ 
            let temperatureThresholds=[12,18,22,26,34];
            return this.getColorType(val,temperatureThresholds);
        },
        getHumidityColorType(val){
            let humidityThresholds = [25,30,35,40,50];
            return this.getColorType(val,humidityThresholds);
        },
        getColorType(val,thresholds){
            if(val<=thresholds[0]){
                return 0;
            } else if(val<=thresholds[1]){
                return 1;
            } else if(val<=thresholds[2]){
                return 2;
            } else if(val<=thresholds[3]){
                return 3;
            } else if(val<=thresholds[4]){
                return 4;
            } else {
                return 5;
            } 
        },
        getDescription(data){
            let des = "<table class=\"cesium-infoBox-defaultTable\">";
            for(let name in data){
                if(!('locations'==name) && !('startTime'==name) && !('endTime'==name)){
                    des +=`<tr><td>${name}</td><td>${data[name]}</td></tr>`;
                }
            }
            des +="</table>";
            return des;
        },
        loadDataFromArray(obj){
            if(this.timeouts != null) {
                this.timeouts.forEach((id)=>{
                    window.clearTimeout(id);
                })
            }
            this.timeouts = [];

            let polyConfig = {//没有移为成员变量是因为发现 vue 的成员变量很耗内存，性能会大大降低
                width:new Cesium.ConstantProperty(2),
                distanceDisplayConditions: [ new Cesium.DistanceDisplayCondition(0.0, 2e5),
                    new Cesium.DistanceDisplayCondition(0.0, 5000)
                ]
            };

            if(this.IsHumidityShown){
                polyConfig.materials = [ new Cesium.ColorMaterialProperty(Cesium.Color.ORANGERED),
                    new Cesium.ColorMaterialProperty(Cesium.Color.GOLD.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.YELLOW.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.WHITESMOKE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.LIGHTSKYBLUE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.DODGERBLUE.withAlpha(0.5))
                ];
            } else if(this.IsTemperatureShown){
                polyConfig.materials = [ new Cesium.ColorMaterialProperty(Cesium.Color.DODGERBLUE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.LIGHTSKYBLUE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.WHITESMOKE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.YELLOW.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.GOLD.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.ORANGERED.withAlpha(0.5))
                ];
            } else {
                polyConfig.materials = [ new Cesium.ColorMaterialProperty(Cesium.Color.GREEN.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.YELLOW.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.ORANGERED.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.RED.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.PURPLE.withAlpha(0.5)),
                    new Cesium.ColorMaterialProperty(Cesium.Color.MAROON.withAlpha(0.5))
                ];
            }

            for(let i=0;i<obj.length;i++){
                let data = obj[i];
                let loc = JSON.parse(obj[i].locations);
                for(let j=0; j < loc.length; j++){
                    let lon = loc[j].longitude;
                    let lat = loc[j].latitude;
                    //test：查看超过范围的数据，这些数据通常是采样错误的数据
                    // let lonf = Number.parseFloat(lon);
                    // let latf = Number.parseFloat(lat); 
                    // if(lonf>120 || lonf < 110 || latf>50 || latf < 30){
                    //     console.log(obj[i].startTime+","+obj[i].lineMark+","+lon+","+lat);
                    // }
                    this.timeouts.push(setTimeout(()=>{
                        if(j%2==0){
                            this.addPointWithHeight(lon,lat,data[this.field],data.startTime,this.getDescription(data),polyConfig,0);
                        } else {
                            this.addPointWithHeight(lon,lat,data[this.field],data.startTime,this.getDescription(data),polyConfig,1);
                        }
                    },i*800+j*80));                    
                }

            }
        },
        loadDataIntoLinesFromArray(obj){//only for global data
            let lines=[],line=[], tracesByUser=[],trace=[];//line显示线，trace 显示动态移动的轨迹点
            let preUser = null;
            let lineC=[]; //test
            let preLineMark = null;
            for(let i=0,length=obj.length;i<length;i++){ 
                let data = obj[i];
                let locations = JSON.parse(data.locations);
                if(data.userId != preUser){
                    trace = [];
                    tracesByUser.push(trace);
                    preUser = data.userId;
                }
                if(preLineMark != data.lineMark ){
                    line = [];
                    lineC ={line:line,id:data.lineMark};
                    lines.push(lineC);
                }
                for(let j=0;j < locations.length;j=j+10){//j++ ->j=j+10 采样加载
                    let loc = locations[j];
                    let lon = parseFloat(loc.longitude);
                    let lat = parseFloat(loc.latitude);
                    let pm25 = parseFloat(data.pm25);
                    let point = Cesium.Cartesian3.fromDegrees(lon,lat,pm25);
                    line.push(point);
                    trace.push(point);
                }                
                preLineMark = data.lineMark;              
            }
            let material =new Cesium.ColorMaterialProperty(Cesium.Color.ORANGERED.withAlpha(0.2)) //纯色线条
            let width =  new Cesium.ConstantProperty(1);
            lines.forEach(lineC=>{
                let line = lineC.line;
                if(line.length<=2){
                    return;
                }
                 let polyline = new Cesium.PolylineGraphics();
                polyline.material = material;
                polyline.width = width;
                polyline.positions = new Cesium.ConstantProperty(line);
                // polyline.distanceDisplayCondition = new Cesium.DistanceDisplayCondition(0.0, 2e5);
                viewerEntities.add({
                    polyline:polyline,
                    name:lineC.id //测试用，可以用来查看是哪一条线
                });
            });
            this.timeouts.push(setTimeout(()=>{
                tracesByUser.forEach(trace=>{
                    let pointEntity = viewerEntities.add({
                        point:{
                            color:Cesium.Color.GREENYELLOW,
                            pixelSize:4,
                            outlineColor:Cesium.Color.GREENYELLOW.withAlpha(0.2),
                            outlineWidth:2
                        },
                        position:trace[0]
                    });
                    //todo：循环播放？
                    for(let i = 1, length = trace.length;i<length;i++){
                        this.timeouts.push(setTimeout(()=>{
                            pointEntity.position = trace[i];
                        },i*1000));
                    }
                });
            },11000));

        },
        removeSpots(){//擦除加载的所有点
            viewerEntities.removeAll();
        },
        addPoint(position,name,description,color) {
            return viewerEntities.add({
                point:{
                    color:color,
                    pixelSize:4,
                    show:true
                },
                name:name,
                description:description,
                position:position
            });
        },
        //立体变色demo：https://cesiumjs.org/Cesium/Build/Apps/Sandcastle/index.html?src=Custom%20DataSource.html
        //时间显示参考：https://groups.google.com/forum/#!topic/cesium-dev/5HhdAYC7ccg
        //https://cesium.com/blog/2018/03/21/czml-time-animation/
        addPointWithHeight(x,y,h,time,description,polyConfig,showMaxHeight){
            let surfacePosition = Cesium.Cartesian3.fromDegrees(x,y,0);
            let polyHeight = h*5+200;
            let heightPosition = Cesium.Cartesian3.fromDegrees(x,y,polyHeight);
            let polyline = new Cesium.PolylineGraphics();
            if(this.IsTemperatureShown) {
                polyline.material = polyConfig.materials[this.getTemperatureColorType(h)];
            } else if(this.IsHumidityShown) {
                polyline.material = polyConfig.materials[this.getHumidityColorType(h)];
            } else {
                polyline.material = polyConfig.materials[this.getPMColorType(h)];
            } 
            polyline.width = polyConfig.width;
            polyline.positions = new Cesium.ConstantProperty([surfacePosition, heightPosition]);
            polyline.distanceDisplayCondition = polyConfig.distanceDisplayConditions[showMaxHeight];
            //设置时间有效性
            // let startTime = Cesium.JulianDate.fromDate(new Date(time));
            // let endTime = Cesium.JulianDate.addDays(startTime,20,new Cesium.JulianDate());
           return viewerEntities.add({
               //点可见时效
                // availability:new Cesium.TimeIntervalCollection([
                //     new Cesium.TimeInterval({
                //         start: startTime,//Cesium.JulianDate.fromIso8601("2019-05-25T10:00:00Z")
                //         stop: endTime
                //     })]),
                polyline:polyline,
                name:time,
                // id:time,
                show:true,
                description:description
            })
        }
    }
}
</script>

<style scoped>
@import url(https://cesiumjs.org/releases/1.61/Build/Cesium/Widgets/widgets.css);
.viewer {
  position:fixed;
  width: 100%;
  height: 100%;
  left:0px;
  top:0px;
  overflow: hidden;
}
.toolbar-left {
    display: block;
    position: absolute;
    z-index: 10;
}
.about-us {
    position: absolute;
    left:0px;
    width:97.5%;
    height: 97.5%;
    z-index: 20;
    opacity:0.9;
}
.legend {
    position: absolute;
    bottom: 34px;
    right: 5px;
    z-index: 10;
    opacity:0.9;
    text-align: left;
}
</style>