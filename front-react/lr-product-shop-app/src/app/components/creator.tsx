'use client';
import { ChangeEvent, FormEvent, useState } from "react";
import { addProduct, Product } from "../server";

const IGNORED_ID = 0;

export default function CreatorForms() {
    const [formData, setFormData] = useState<Partial<Product>>({});
    const [addedId, setAddedId] = useState<number | undefined>();

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value,
        });
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        const res: Product = { id: IGNORED_ID, name: formData.name, producer: formData.producer, count: formData.count }
        const addedId = await addProduct(res)
        setAddedId(addedId);
    };

    const formLabels = ["name", "producer", "count"]
    return (
        <div>
            <form onSubmit={handleSubmit}>
                {formLabels.map(l => <input type="text" name={l} placeholder={l} key={l} onChange={handleChange}></input>)}
                <input type="submit" value="Submit" />
            </form>
            <div>{addedId != null ? "Product added with id: " + addedId : "Product not added."}</div>
        </div>
    )
}
