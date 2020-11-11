<template>
  <div id="option-table">
      <div>
        <label class="chm-label">Volunteer</label>
        <select class="chm-select cesium-button" name="volunteerId" v-model="volunteerId">
          <option v-for="option in vOptions" :value="option.id" v-bind:key="option.id">
            {{option.value}}
          </option>
        </select>
      </div>
      <div>
        <label class="chm-label">Season</label>
        <select class="chm-select cesium-button" name="term" v-model="term">
          <option value="1">Spring</option>
          <option value="2">Summer</option></option>
          <option value="3">Autumn</option>
          <option value="4">Winter</option>
        </select>
      </div>
      <div>
        <label class="chm-label">Attribute</label>
        <select class="chm-select cesium-button" name="field" v-model="field">
          <option value="temperature">Temperature</option>
          <option value="humidity">Humidity</option>
          <option value="pm1">PM1</option>
          <option value="pm25">PM2.5</option>
          <option value="pm10">PM10</option>
        </select>
      </div>
      <!-- <div title="加载更多" class="chm-expand-button"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="16" class="load-more-button" v-on:click="loadMore"><path d="M12.44 6.44L9 9.88 5.56 6.44 4.5 7.5 9 12l4.5-4.5z"/></svg></div> -->
      <div>
          <input class="chm-submit cesium-button" type="submit" v-on:click="submitChoice" value=" Load ">
      </div>
  </div>
</template>

<script>

//form input binding: https://vuejs.org/v2/guide/forms.html
//communication between parent anc child component: https://gambardella.info/2017/09/13/vue-js-communication-part-2-parent-child-components/

export default {
  name: 'OptionTable',
  data:function(){
    return {
    volunteerId:"001",
    term:"1",
    field:"pm25",
    volunteerIds:[],
    }
  },
  computed: {
    vOptions(){
      for(let i = 1; i <=71; i++){
        this.volunteerIds.push({
          id:i.toString().padStart(3,"0"),
          value:`Vol No.${i}`});
      }
      return this.volunteerIds;
    }

  },
  methods:{
    submitChoice(){
      this.$emit("submitchoice",{volunteerId:this.volunteerId,term:this.term,field:this.field});
    },
    // loadMore(){

    // }
  }
  
}
</script>

<style>
#option-table {
  display: flex;
  justify-content: space-around;
  margin:10px;
  opacity:0.8;
  align-items: center;
}

.chm-label {
  margin:5px;
  font-size:small;
}

.chm-select {
  margin:5px;
  padding:5px.
}

.chm-expand-button{
  margin:3px 0;
  height:20px;
}

.load-more-button{
  border-style: solid;
  border-color: lightgray;
  border-width: 2px;
  border-radius: 5px;
  margin:2px;
}

.chm-submit {
  margin:5px;
  font-size:smaller;
}
 
</style>