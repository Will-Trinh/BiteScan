const axios = require("axios");

module.exports = async function googleSearch(query) {
    const apiUrl = "https://www.googleapis.com/customsearch/v1";

    const res = await axios.get(apiUrl, {
        params: {
            key: process.env.GOOGLE_API_KEY,
            cx: process.env.GOOGLE_CX_ID,
            q: query
        }
    });

    const items = res.data.items || [];
    return items.slice(0, 5).map(i => i.link); // top 5 links
};
