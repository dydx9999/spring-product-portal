document.querySelectorAll(".product-row").forEach((productRow) => {
    const priceElement = productRow.querySelector(".product-price");
    const discountRange = productRow.querySelector(".form-range");
    const discountedPriceOutput = productRow.querySelector(".discounted-price");

    function updateDiscountedPrice() {
        const priceCents = Math.round(Number(
            priceElement.textContent.replace(/[$,]/g, "").trim()
        ) * 100);
        const discountPercent = Number(discountRange.value);
        const discountedPrice = Math.round(priceCents * (100 - discountPercent) / 100) / 100;

        discountedPriceOutput.textContent = `$${discountedPrice.toFixed(2)}`;
        discountedPriceOutput.style.color = 'red';

    }

    discountRange.addEventListener("input", updateDiscountedPrice);
});