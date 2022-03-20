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
        player_id = 1 + Math.round(Math.random() * 9)
        product_id = 1 + Math.round(Math.random() * 9)
        score = Math.round(Math.random() * 100)
        return {
            score,
            product_id,
            player_id
        }
    }

    function generateNObjects(n) {
        for (x = 0; x < n; x++) {
            console.log(JSON.stringify(createObject(x)))
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