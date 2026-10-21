document.querySelectorAll(".product-row").forEach((productRow) => {
    const priceElement = productRow.querySelector(".product-price");
    const discountRange = productRow.querySelector(".form-range");
    const discountedPriceOutput = productRow.querySelector(".discounted-price");

    function updateDiscountedPrice() {
        const currentPrice = Number(
            priceElement.textContent.replace(/[$,]/g, "").trim()
        );
        const discountPercent = Number(discountRange.value);
        const discountedPrice = currentPrice * (1 - discountPercent / 100);

        discountedPriceOutput.textContent = `$${discountedPrice.toFixed(2)}`;
        discountedPriceOutput.style.color = 'red';

    }

    discountRange.addEventListener("input", updateDiscountedPrice);
    updateDiscountedPrice();
});