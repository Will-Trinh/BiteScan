function buildRecipeQuery(ingredients, country) {
  const ing = ingredients.join(" ");

  return `
    best ${country} recipes using ${ing} |
    how to cook ${ing} ${country} style |
    ${country} cuisine recipe with ${ing} |
    easy recipe with ${ing} ${country} |
    authentic ${country} recipe made with ${ing}
  `;
}

module.exports = { buildRecipeQuery };
