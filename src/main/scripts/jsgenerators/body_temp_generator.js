function generate() {
    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    function createObject(x) {
        anobj = {}
        anobj.timestamp = new Date()
        anobj.temperature = 97 + Math.round(Math.random() * 40) / 10
        anobj.unit = 'F'
        return anobj
    }

    async function generateObjects() {
        let x = 0
        while (x < 1) {
            console.log(Math.round(1 + Math.random() * 10) + ',' + JSON.stringify(createObject()))
            await sleep(1000)
        }
    }
    generateObjects()
}
generate()