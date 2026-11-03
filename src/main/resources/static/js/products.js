const output = document.querySelector(".output");
const comparisonHeader = document.querySelector(".comparison-header");
const comparisonSubmitButton = document.querySelector(".comparison-submit");
const searchbar = document.getElementById('searchbar');
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

function showPriceEditField(row) {
    const form = row.querySelector('.price-edit-form');
    const price = row.querySelector('.product-price');
    const editButton = row.querySelector('.price-edit-button');
    const input = form?.querySelector('input');

    if (!form || !price || !editButton || !input) {
        return;
    }

    form.hidden = false;
    price.hidden = true;
    editButton.hidden = true;
    input.focus();
    input.select();
}

function hidePriceEditField(row) {
    const form = row.querySelector('.price-edit-form');
    const price = row.querySelector('.product-price');
    const editButton = row.querySelector('.price-edit-button');
    const input = form?.querySelector('input');

    if (!form || !price || !editButton || !input) {
        return;
    }

    input.value = price.textContent.trim().replace(/^\$/, '');
    form.hidden = true;
    price.hidden = false;
    editButton.hidden = false;
}

function initializePriceEditing() {
    document.querySelectorAll('.product-row').forEach(row => {
        row.querySelector('.price-edit-button')
        ?.addEventListener('click', () => showPriceEditField(row));
        row.querySelector('.price-edit-cancel')
        ?.addEventListener('click', () => hidePriceEditField(row));
    });
}
function searchProducts() {
    const query = searchbar.value.trim().toLowerCase();
    document.querySelectorAll('.product-row').forEach(row => {
        const productName = row.querySelector('.product-name')?.textContent.toLowerCase() ?? '';
        row.hidden = !productName.includes(query);
    });
}

searchbar?.addEventListener('input', searchProducts);



initializePriceEditing();

