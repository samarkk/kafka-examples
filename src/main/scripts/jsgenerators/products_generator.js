function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
games = ['Beat Saber', 'Pistol Whip', 'Moss', 'The Room', 'Population One', 'Power Beats', "Audio Trip", 'Thrill of the Fight', 'Pavlov VR', 'Gorn']

function createProduct(x) {
    anProduct = {}
    anProduct.id = x + 1
    anProduct.name = games[x]
    return anProduct
}

function generateNProducts(n) {
    for (x = 0; x < games.length; x++) {
        console.log((x + 1) + ',' + JSON.stringify(createProduct(x)))
    }
}

generateNProducts(10)