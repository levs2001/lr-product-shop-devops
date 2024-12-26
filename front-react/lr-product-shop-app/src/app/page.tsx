import CreatorForms from "./components/creator";
import SearchForms from "./components/search";
import styles from "./page.module.css";

export default function Home() {
  return (
    <div className={styles.page}>
      <h1>Продуктовый магазин</h1>
      <div className={styles.creator}>
        <h2>Добавление</h2>
        <CreatorForms />
      </div>
      <div className={styles.search}>
        <h2>Поиск</h2>
        <SearchForms />
      </div>
    </div>
  );
}
