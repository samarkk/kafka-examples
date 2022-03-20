function generate() {
    // self explanatory arguments below
    // noOfObjectsToGenerate, 
    // no of times - cycles, to generate
    // sleepDur - sleep between cycles
    noOfObjectsToGenerate = +process.argv[2]
    cycles = +process.argv[3]
    sleepDur = +process.argv[4]

    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    function createObject(x) {
        anobject = {}
        anobject.timestamp = new Date()
        return anobject
    }

    function generateNObjects(n) {
        for (x = 0; x < n; x++) {
            console.log((x + 1) + ',' + JSON.stringify(createObject(x)))
        }
    }

    async function generateDataCycles() {
        iterator = 0
        while (iterator < cycles) {
            generateNObjects(noOfObjectsToGenerate)
            iterator += 1
            await sleep(sleepDur)
        }
    }
    generateDataCycles()
}
generate()