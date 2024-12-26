'use client';
import { ChangeEvent, FormEvent, useState } from "react";
import { Product } from "../server";
import { searchProduct, searchProductWithId } from "../server";

interface SearchData {
  id: number;
  name: string;
}

async function search(searchData: Partial<SearchData>) {
  try {
    console.log("form data:", searchData)
    const json = JSON.stringify(searchData)
    console.log("Json: ", json)
    if (searchData.id){
      console.log("Search with id")
      const response = searchProductWithId(searchData.id);
      return response;      
    }
    if (searchData.name) {
      console.log("Search with text")
      const response = searchProduct(searchData.name);
      return response;
    }
  } catch (error) {
    console.error('Error:', error);
  }
  return []
}

export default function SearchForms() {
  const [searchData, setSearchData] = useState<Partial<SearchData>>({});
  const [searchResult, setSearchResult] = useState<Product[]>([]);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setSearchData({
      ...searchData,
      [name]: value,
    });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    const result = await search(searchData);
    setSearchResult(result)
    console.log("Result sd: ", result)
  };

  const formLabels = ["id", "name"]
  return (
    <form onSubmit={handleSubmit}>
      {formLabels.map(l => <input type="text" name={l} placeholder={l} key={l} onChange={handleChange}></input>)}
      <input type="submit" value="Search" />
      <table>
        <thead>
          <tr>
            <th>Id</th>
            <th>Name</th>
            <th>Producer</th>
            <th>Count</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {searchResult.map(r => (
            <tr>
              <td>{String(r.id)}</td>
              <td>{r.name}</td>
              <td>{r.producer}</td>
              <td>{String(r.count)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </form>
  )
}