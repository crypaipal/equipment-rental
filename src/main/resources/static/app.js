const resultBox = document.getElementById("result");

function showResult(data) {
    resultBox.textContent = JSON.stringify(data, null, 2);
}

async function apiRequest(url, method = "GET", body = null) {
    const options = {
        method,
        headers: {}
    };

    if (body !== null) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(url, options);
        const text = await response.text();

        let data;
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = text;
        }

        if (!response.ok) {
            showResult(data);
            return null;
        }

        showResult(data);
        return data;
    } catch (error) {
        showResult({
            error: error.message
        });
        return null;
    }
}

function setValue(id, value) {
    if (value) {
        document.getElementById(id).value = value;
    }
}

// Inventory

async function registerModel() {
    const data = await apiRequest("/api/inventory/models", "POST", {
        name: document.getElementById("modelName").value,
        category: document.getElementById("modelCategory").value,
        manufacturer: document.getElementById("modelManufacturer").value
    });

    if (data) {
        setValue("assetModelId", data.id);
    }
}

async function registerAsset() {
    const data = await apiRequest("/api/inventory/assets", "POST", {
        equipmentModelId: document.getElementById("assetModelId").value,
        inventoryTag: document.getElementById("assetInventoryTag").value
    });

    if (data) {
        setValue("conditionAssetId", data.id);
        setValue("reservationAssetId", data.id);
    }
}

async function changeAssetCondition() {
    const assetId = document.getElementById("conditionAssetId").value;

    await apiRequest(`/api/inventory/assets/${assetId}/condition`, "PATCH", {
        condition: document.getElementById("assetCondition").value,
        damageReport: document.getElementById("damageReport").value || null
    });
}

// Identity

async function registerUser() {
    const data = await apiRequest("/api/identity/register", "POST", {
        firstName: document.getElementById("firstName").value,
        lastName: document.getElementById("lastName").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value,
        role: document.getElementById("role").value
    });

    if (data) {
        setValue("reservationUserId", data.id);
        setValue("loginEmail", data.email);
    }
}

async function loginUser() {
    await apiRequest("/api/identity/login", "POST", {
        email: document.getElementById("loginEmail").value,
        password: document.getElementById("loginPassword").value
    });
}

// Rental

async function findAvailableAssets() {
    const category = encodeURIComponent(document.getElementById("searchCategory").value);
    const periodFrom = encodeURIComponent(document.getElementById("searchFrom").value);
    const periodTo = encodeURIComponent(document.getElementById("searchTo").value);

    const data = await apiRequest(
        `/api/rental/available-assets?category=${category}&periodFrom=${periodFrom}&periodTo=${periodTo}`
    );

    if (Array.isArray(data) && data.length > 0) {
        setValue("reservationAssetId", data[0].assetId);
        setValue("conditionAssetId", data[0].assetId);
    }
}

async function requestReservation() {
    const data = await apiRequest("/api/rental/reservations", "POST", {
        userId: document.getElementById("reservationUserId").value,
        assetId: document.getElementById("reservationAssetId").value,
        periodFrom: document.getElementById("reservationFrom").value,
        periodTo: document.getElementById("reservationTo").value
    });

    if (data) {
        setValue("reviewReservationId", data.id);
        setValue("checkoutReservationId", data.id);
    }
}

async function approveReservation() {
    const reservationId = document.getElementById("reviewReservationId").value;

    const data = await apiRequest(
        `/api/rental/reservations/${reservationId}/approve`,
        "POST"
    );

    if (data) {
        setValue("checkoutReservationId", data.id);
    }
}

async function rejectReservation() {
    const reservationId = document.getElementById("reviewReservationId").value;

    await apiRequest(`/api/rental/reservations/${reservationId}/reject`, "POST", {
        rejectionReason: document.getElementById("rejectionReason").value
    });
}

async function checkoutEquipment() {
    const reservationId = document.getElementById("checkoutReservationId").value;

    const data = await apiRequest(
        `/api/rental/reservations/${reservationId}/checkout`,
        "POST"
    );

    if (data) {
        setValue("returnRentalId", data.id);
    }
}

async function cancelReservation() {
    const reservationId = document.getElementById("checkoutReservationId").value;

    await apiRequest(
        `/api/rental/reservations/${reservationId}/cancel`,
        "POST"
    );
}

async function returnEquipment() {
    const rentalId = document.getElementById("returnRentalId").value;
    const returnedAtValue = document.getElementById("returnedAt").value;

    const body = {
        damaged: document.getElementById("returnDamaged").value === "true",
        damageReport: document.getElementById("returnDamageReport").value || null,
        returnedAt: returnedAtValue || null
    };

    await apiRequest(`/api/rental/rentals/${rentalId}/return`, "POST", body);
}