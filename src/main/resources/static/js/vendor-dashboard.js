function showEditField(row) {
    const form = row.querySelector('.name-edit-form');
    const name = row.querySelector('.product-name');
    const editButton = row.querySelector('.name-edit-button')

    form.hidden = false;
    editButton.hidden = true;
    name.hidden = true;
    form.querySelector('input').focus();
}

function cancelEditField(row) {
    const form = row.querySelector('.name-edit-form');
    const name = row.querySelector('.product-name');
    const editButton = row.querySelector('.name-edit-button')


    form.querySelector('input').value = name.textContent.trim();
    form.hidden = true;
    name.hidden = false;
    editButton.hidden = false;

}

function initializeNameEditing() {
    document.querySelectorAll('.product-row').forEach(row => {
        row.querySelector('.name-edit-button')
            .addEventListener('click', () => showEditField(row));

        row.querySelector('.name-edit-cancel')
            .addEventListener('click', () => cancelEditField(row));
    });
}

initializeNameEditing();