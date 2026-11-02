const productName = document.getElementById("productName");
const productNameCharacterCount = document.getElementById("productNameCharacterCount");
displayCharacterCount();
const productPrice = document.getElementById("productPrice");
const productQuantity = document.getElementById("productQuantity");
const lineTotal = document.getElementById("lineTotal");
const warehouseButton = document.querySelector(".warehouse-add-button");
const warehouseInput = document.querySelector(".warehouse-location");
const warehouseContainer = document.querySelector(".warehouse-container");
let warehouseFieldCount = 1;



function displayCharacterCount() {
    const maxCharacters = 250;

    productName.addEventListener("input", (event) => {

        const currentValue = productName.value;
        const currentCharacterCount = currentValue.length;
        const remainingCharacters = maxCharacters - currentCharacterCount;
        if (remainingCharacters >= 0) {

            productNameCharacterCount.textContent =
                remainingCharacters + " characters remaining";
        };


        productNameCharacterCount.hidden = (remainingCharacters >= 250);
        if (remainingCharacters < 0) {

            productNameCharacterCount.textContent = "Character limit exceeded";

            productNameCharacterCount.style.color = 'red';
        };
    })
};

function calculateTotalUnitPrice() {

    if (productPrice.value === "" || productQuantity.value === "") {

        lineTotal.textContent = "Enter price and quantity";
        return;
    }


    const price = Number(productPrice.value);

    const quantity = Number(productQuantity.value);
    const total = price * quantity;

    lineTotal.textContent = `$${total.toFixed(2)}`;
}


// @
warehouseButton.addEventListener("click", () => {

    const warehouseField = warehouseInput.cloneNode(true);

    const select = warehouseField.querySelector("select");

    const label = warehouseField.querySelector("label");
    const fieldIndex = warehouseFieldCount++;
    const fieldId = `warehouseLocation-${fieldIndex}`;
    const fieldRow = document.createElement("div");
    const deleteButton = document.createElement("button");
    const quantityFloatingField = document.createElement("div");
    const quantityField = document.createElement("input");
    const quantityLabel = document.createElement("label");
    quantityFloatingField.className = "form-floating flex-grow-1";
    quantityField.id = `warehouseQuantity-${fieldIndex}`;
    quantityField.className = "form-control";
    quantityField.type = "number";
    quantityField.placeholder = " ";
    quantityField.step = "1";
    quantityLabel.htmlFor = quantityField.id;
    quantityLabel.textContent = "Quantity";
    quantityFloatingField.append(quantityField, quantityLabel);
    select.id = fieldId;
    label.htmlFor = fieldId;

    warehouseField.classList.remove("mb-4");

    warehouseField.classList.add("flex-grow-1");
    fieldRow.className = "d-flex align-items-center gap-2 mb-3";
    deleteButton.type = "button";
    deleteButton.className = "btn btn-outline-danger";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => fieldRow.remove());

    fieldRow.append(warehouseField, quantityFloatingField, deleteButton);

    warehouseContainer.appendChild(fieldRow);
});



// @
productPrice.addEventListener("input", calculateTotalUnitPrice);
// @
productQuantity.addEventListener("input", calculateTotalUnitPrice);
calculateTotalUnitPrice();
