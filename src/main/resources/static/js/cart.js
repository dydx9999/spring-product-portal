
const container = document.querySelector("#container");
const checkout = document.getElementById("checkout");

container.addEventListener("input", (event) => {


    if (event.target.classList.contains("form-quantity")) {
        const currentInput = event.target;

        const currentValue = currentInput.value;
        // 3. Find the parent row container for THIS specific product

        const productRow = currentInput.closest(".product-row");
        const cartCounter = productRow.querySelector(".cart-item-quantity");
        const availableQuantity = productRow.querySelector(
            ".product-item-quantity",
        );
        const output = productRow.querySelector(".output");

        // 5. Update only that specific counter
        cartCounter.textContent = Number(currentValue);
        calculateLineTotal(productRow);
        if (Number(currentValue) > Number(availableQuantity.textContent)) {
            cartCounter.style.color = "red";
            output.textContent = "Invalid quantity";
        } else {
            output.textContent = "";

            cartCounter.style.color = "black";
        };
    };
});
function calculateLineTotal(productRow) {
    const lineTotal = productRow.querySelector(".line-total");
    const cartQuantity = productRow.querySelector(".cart-item-quantity");
    const productPrice = productRow.querySelector(".product-price");
    const total = Number(productPrice.textContent) * Number(cartQuantity.textContent);
    lineTotal.textContent = total.toLocaleString("en-US", {
        style: "currency",
        currency: "USD",
    });
}



