const HelloVueApp = {
    data() {
        return {
                json: undefined
            }   
    },
    mounted() {
        console.log("Mounted")
    },
    methods: {
        doAjax() {
            fetch("api/categories")
                .then(response => response.json())
                .then(json => { this.json = json})
                .catch(error => alert(error));            
        }
    }
  }
  
Vue.createApp(HelloVueApp).mount('#app')