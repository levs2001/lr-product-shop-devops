export interface Product {
    id?: number,
    name?: string,
    producer?: string,
    count?: number
}

const MY_SERVER = process.env.NEXT_PUBLIC_BACKEND_URL;
console.log("my server: " + MY_SERVER)
export async function addProduct(product: Product): Promise<number | undefined> {
    const json = JSON.stringify(product)

    const response = await fetch(MY_SERVER + "product-shop/add-product/", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: json,
    });

    if (response.ok) {
        const result = await response.json();
        console.log('Success:', result);
        return result;
    } else {
        console.error('Error:', response.statusText);
    }
}

export async function searchProduct(name: string): Promise<Product[]> {
    // /product-shop/search-product/
    const query = MY_SERVER + "product-shop/search-product/?name=" + name 
    const ans = await fetch(query,
        {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        }
    );
    const json = await ans.json();
    console.log("Search json: ", json);
    const result: Product[] = json; 
    console.log("Search result: ", result)
    return result;
}

export async function searchProductWithId(id: number): Promise<Product[]> {
    // /product-shop/search-product/
    const query = MY_SERVER + "product-shop/get-product/?id=" + id 
    const ans = await fetch(query,
        {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        }
    );
    const json = await ans.json();
    console.log("Search json: ", json);
    const result: Product = json; 
    console.log("Search result: ", result)
    return [result];
}