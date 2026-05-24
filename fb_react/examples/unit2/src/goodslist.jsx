import { ProductList } from "./listdata";
import { FruitList } from "./listdata";
function showGoods(){
    return(
        <div>
           <FruitList/>
           <ProductList/>
        </div>
    )
}
export default showGoods;