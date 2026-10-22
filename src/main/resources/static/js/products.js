const output = document.querySelector(".output");
const comparisonHeader = document.querySelector(".comparison-header");
const comparisonSubmitButton = document.querySelector(".comparison-submit");
if (output) {
    const comparisonList = document.createElement("ul");
    comparisonList.className = "comparison-list";
    output.appendChild(comparisonList);

    document.querySelectorAll(".product-row").forEach((productRow) => {

        const comparisonCheckbox = productRow.querySelector(".product-comparison");
        const productNameElement = productRow.querySelector(".product-name");


        if (!comparisonCheckbox || !productNameElement) {
            return;
        }

        const productId = comparisonCheckbox.dataset.productId;
        const productName = productNameElement.textContent.trim();

        comparisonCheckbox.addEventListener("change", () => {
            const existingItem = comparisonList.querySelector(
                `[data-product-id="${productId}"]`
            );


            if (comparisonCheckbox.checked) {
                if (!existingItem) {
                    const comparisonListItem = document.createElement("li");
                    comparisonListItem.dataset.productId = productId;
                    comparisonListItem.textContent = productName;
                    comparisonList.appendChild(comparisonListItem);
                    comparisonHeader.textContent = "Currently selected for comparison (" + comparisonList.childElementCount + "):";
                }
                console.log("Selected product for comparison: " + productName);
            } else {
                existingItem?.remove();
                comparisonHeader.textContent = "Currently selected for comparison (" + comparisonList.childElementCount + "):";
                console.log("Removed product from comparison: " + productName);

            };
            if (comparisonHeader) {
                comparisonHeader.hidden = comparisonList.children.length === 0;
                comparisonSubmitButton.hidden = comparisonList.children.length === 0;
            }

        });
    });
}

