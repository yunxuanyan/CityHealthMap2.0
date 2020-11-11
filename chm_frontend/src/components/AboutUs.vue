<template>
    <div class="about-us-container" v-if="isAboutUsShown">
        <div class="passage">
            <p class="about-us-passage">
                Air quality is an important factor to define how clear the urban environment is. People pay more and more attentions on air quality index of their surrounding, such as PM10, PM2.5, and even PM1. However, environments of different suburbs in a large city is too diversity that one single index can't be used to describe the whole city. 
            </p>
            <p class="about-us-passage">
                City Health Map aims to get you a more detailed perspective to view the air quality in where you live. We involved a number of volunteers to wear a mobile air quality monitoring sensor with GPS to collect data from their daily lives. This website is to show what we have got in Beijing of 2018.
            </p>
        </div>
        <div>
            <input class="chm-OK cesium-button" type="submit" v-on:click="ClickOK" value="Explore">
        </div>
    </div>
</template>

<script>
export default {
    name:'AboutUs',
    data:function(){
        return {
            isAboutUsShown:true
        }
    },
    methods:{
        ClickOK(){
            this.isAboutUsShown = false;
        }
    }
}
</script>

<style scoped>
.about-us-container {
    display:flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}
.passage {
    width:50%;
    text-align: left;
}
.about-us-passage {
    font-size:larger;
    text-align: justify;
}
.chm-OK {
    font-size:larger;
    width:150px;
    border:1px solid;
}
</style>