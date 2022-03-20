function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function createObject(x) {
    anObject = {}
    anObject.id = x
    anObject.name = 'Player' + x
    return anObject
}

function generateNObjects(n) {
    for (x = 0; x < n; x++) {
        console.log(JSON.stringify(createObject(x)))
    }
}

async function generateDataCycles(cycles) {
    iterator = 0
    while (iterator < cycles) {
        generateNObjects(10)
        iterator += 1
        await sleep(1000)
    }
}
generateDataCycles(1)