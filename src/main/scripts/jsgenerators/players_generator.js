function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function createPlayer(x) {
    anPlayer = {}
    anPlayer.id = x + 1
    anPlayer.name = 'Player' + (x + 1)
    return anPlayer
}

function generateNPlayers(n) {
    for (x = 0; x < n; x++) {
        console.log((x + 1) + ',' + JSON.stringify(createPlayer(x)))
    }
}

generateNPlayers(10)